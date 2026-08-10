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
# The only artifact that distinguishes 146 tests from 0 is <testsuite tests="N">
# in the JUnit XML, so that is what this reads. Comparing against the nodejs count
# for the same target additionally catches a partial discovery, where the browser
# runs some suites but silently drops others.
#
# Usage: scripts/ci/assert_browser_tests_ran.sh <variant>    # js | wasmJs
set -euo pipefail

VARIANT="${1:?usage: assert_browser_tests_ran.sh <js|wasmJs>}"
MODULES=(kcsg kcsg-dsl)

# Sums the tests="N" attributes across a task's JUnit XML. Only <testsuite>
# carries that attribute, so <testcase> elements cannot inflate the total.
count_tests() {
    local dir="$1"
    if [ ! -d "$dir" ]; then
        echo "-1"
        return
    fi
    local total
    total="$(
        grep -ho 'tests="[0-9]\+"' "$dir"/*.xml 2>/dev/null |
            grep -o '[0-9]\+' |
            awk '{ sum += $1 } END { print sum + 0 }'
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
            "success without running anything (no browser, or empty bundle)."
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
