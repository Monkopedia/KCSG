#!/usr/bin/env bash
# Negative control for scripts/ci/assert_browser_tests_ran.sh.
#
# WHY THIS EXISTS: that script is a gate, and a gate nobody has watched fail is
# indistinguishable from one that cannot fail. This repo has shipped several of
# those. So this feeds the gate synthetic JUnit XML and requires it to go RED on
# each way it is supposed to, and GREEN on a healthy suite -- the green case is
# the positive control, without which "everything failed" would also pass.
#
# The all-skipped case is the one that matters: <testsuite tests="3" skipped="3">
# is what an entirely @Ignore'd suite emits, and a gate reading tests="N" alone
# scores it 3 and passes.
#
# Usage: scripts/ci/test_assert_browser_tests_ran.sh
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
GATE="$ROOT/scripts/ci/assert_browser_tests_ran.sh"
WORK="$(mktemp -d "${TMPDIR:-/tmp}/kcsg-gate-selftest-XXXXXX")"
trap 'rm -rf "$WORK"' EXIT

# tests / skipped per suite, written where the gate looks for them.
write_suite() {
    local dir="$1" name="$2" tests="$3" skipped="$4"
    mkdir -p "$dir"
    printf '<testsuite name="%s" tests="%s" skipped="%s" failures="0" errors="0"></testsuite>\n' \
        "$name" "$tests" "$skipped" > "$dir/TEST-$name.xml"
}

# Lays down both modules for one variant, then runs the gate from $WORK.
run_case() {
    local browser_tests="$1" browser_skipped="$2" node_tests="$3" node_skipped="$4"
    rm -rf "$WORK/kcsg" "$WORK/kcsg-dsl"
    local m
    for m in kcsg kcsg-dsl; do
        write_suite "$WORK/$m/build/test-results/jsBrowserTest" "B$m" "$browser_tests" "$browser_skipped"
        write_suite "$WORK/$m/build/test-results/jsNodeTest"    "N$m" "$node_tests"    "$node_skipped"
    done
    ( cd "$WORK" && bash "$GATE" js >/dev/null 2>&1 )
    echo $?
}

fail=0
check() {
    local label="$1" expected="$2" actual="$3"
    if [ "$actual" = "$expected" ]; then
        echo "  ok   $label (exit $actual)"
    else
        echo "  FAIL $label: expected exit $expected, got $actual"
        fail=1
    fi
}

echo "assert_browser_tests_ran.sh self-test"

# Positive control. If this does not pass, every RED below is meaningless.
check "healthy suite passes"                    0 "$(run_case 5 0 5 0)"

# The finding this file was written for.
check "all-skipped browser suite FAILS"         1 "$(run_case 5 5 5 0)"

# The other side of the tests-minus-skipped boundary, and the only case that
# exercises the subtraction toward PASS. Without it, `sum += (s > 0 ? 0 : t)` and
# `sum += t - s - 1` both survive this whole file while wrongly rejecting a
# legitimately mostly-skipped suite -- a gate that is wrong in the direction that
# looks like strictness, and gets blamed on flaky CI rather than on itself.
check "mostly-skipped matching pair PASSES"     0 "$(run_case 5 4 5 4)"
check "partially-skipped mismatch FAILS"        1 "$(run_case 5 2 5 0)"

# Pre-existing behaviours, kept under control so a refactor cannot drop them.
check "zero-test browser suite FAILS"           1 "$(run_case 0 0 5 0)"
check "browser/node count mismatch FAILS"       1 "$(run_case 3 0 5 0)"
check "both all-skipped still FAILS"            1 "$(run_case 5 5 5 5)"

if [ "$fail" -ne 0 ]; then
    echo "self-test FAILED -- the browser-execution gate does not behave as documented."
    exit 1
fi
echo "self-test passed."
