#!/usr/bin/env bash
# Asserts that a browser test task actually executed tests, and executed the same
# number of them as its nodejs counterpart.
#
# WHY THIS EXISTS: karma fails soft. The karma.conf.js that the Kotlin Gradle
# plugin generates sets both "failOnFailingTestSuite": false and
# "failOnEmptyTestSuite": false, so a run that discovers no tests at all — because
# the browser never launched, or the webpack bundle came up empty — reports
# success. Gradle then prints nothing, because it prints "N tests completed" only
# on FAILURE. A green browser leg is therefore compatible with zero tests having
# run, which is strictly worse than having no browser leg: it converts an unknown
# into a false assurance.
#
# The only artifact that distinguishes 146 tests from 0 is the JUnit XML, so that
# is what this reads. It counts tests="N" MINUS skipped="M": `tests` on its own
# includes skipped testcases, so a suite whose tests are all @Ignore'd reports a
# full, healthy N and would sail through a gate that read `tests` alone -- the
# same "green proves nothing" failure this script exists to catch, one layer down.
# Comparing against the nodejs count for the same target additionally catches a
# partial discovery, where the browser runs some suites but silently drops others.
#
# scripts/ci/test_assert_browser_tests_ran.sh is the negative control for all of
# the above: it feeds this script synthetic XML and requires it to go RED.
#
# Usage: scripts/ci/assert_browser_tests_ran.sh <variant>    # js | wasmJs
set -euo pipefail

VARIANT="${1:?usage: assert_browser_tests_ran.sh <js|wasmJs>}"
MODULES=(kcsg kcsg-dsl)

# Sums EXECUTED tests -- tests="N" minus skipped="M" -- across a task's JUnit XML.
# Both attributes live on the same <testsuite> element, and only <testsuite>
# carries them, so <testcase> elements cannot inflate the total. Subtracting
# `skipped` is the whole point: `tests` counts @Ignore'd cases, so reading it
# alone proves a suite was COLLECTED, not that anything RAN.
count_tests() {
    local dir="$1"
    if [ ! -d "$dir" ]; then
        echo "-1"
        return
    fi
    local total
    total="$(
        grep -ho '<testsuite [^>]*>' "$dir"/*.xml 2>/dev/null |
            awk '
                {
                    t = 0; s = 0
                    if (match($0, /tests="[0-9]+"/))   { t = substr($0, RSTART + 7, RLENGTH - 8) }
                    if (match($0, /skipped="[0-9]+"/)) { s = substr($0, RSTART + 9, RLENGTH - 10) }
                    sum += t - s
                }
                END { print sum + 0 }
            '
    )"
    echo "${total:-0}"
}

status=0
for module in "${MODULES[@]}"; do
    browser_dir="${module}/build/test-results/${VARIANT}BrowserTest"
    node_dir="${module}/build/test-results/${VARIANT}NodeTest"
    browser_count="$(count_tests "$browser_dir")"
    node_count="$(count_tests "$node_dir")"

    if [ "$browser_count" -lt 0 ]; then
        echo "FAIL ${module} ${VARIANT}Browser: no test results at ${browser_dir}"
        status=1
        continue
    fi
    if [ "$node_count" -lt 0 ]; then
        echo "FAIL ${module} ${VARIANT}Node: no test results at ${node_dir}"
        status=1
        continue
    fi

    echo "${module} ${VARIANT}: browser=${browser_count} node=${node_count}"

    if [ "$browser_count" -eq 0 ]; then
        echo "FAIL ${module} ${VARIANT}BrowserTest executed 0 tests -- karma reported" \
            "success without running anything (no browser, empty bundle, or every" \
            "test skipped)."
        status=1
    elif [ "$browser_count" -ne "$node_count" ]; then
        echo "FAIL ${module} ${VARIANT}BrowserTest executed ${browser_count} tests but" \
            "${VARIANT}NodeTest executed ${node_count}. The browser leg is not covering" \
            "the same suites; fix the gap or state why the counts differ."
        status=1
    fi
done

if [ "$status" -ne 0 ]; then
    echo "Browser execution assertion failed for ${VARIANT}."
else
    echo "Browser execution assertion passed for ${VARIANT}."
fi
exit "$status"
