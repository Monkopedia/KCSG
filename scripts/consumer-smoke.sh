#!/usr/bin/env bash
#
# Consumer resolution smoke test -- issue #51.
#
# apiCheck validates the shape of the public ABI. It has no view of whether somebody who
# resolves the published artifact can NAME the types in that ABI. That is a property of the
# Gradle module metadata (which dependencies land in `*ApiElements` vs `*RuntimeElements`),
# and the only instrument that sees it is an actual consumer resolving an actual publication.
#
# So: publish :kcsg and :kcsg-dsl to a throwaway file repository, then build ../consumer-smoke
# -- a separate Gradle build whose two modules each depend on ONE of the published coordinates
# and on nothing else -- and compile source that names the types those APIs hand back.
#
# JVM only. The break is confined to the jvm/js/wasm variants (Kotlin/Native needs the full
# transitive graph, so KGP publishes `implementation` deps into `*ApiElements` there anyway),
# and JVM is the coordinate consumers actually use.
#
# Usage: ./scripts/consumer-smoke.sh
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REPO_DIR="$ROOT/build/consumer-smoke-repo"
CONSUMER_DIR="$ROOT/consumer-smoke"

VERSION="$(sed -n 's/^[[:space:]]*version[[:space:]]*=[[:space:]]*//p' "$ROOT/gradle.properties" \
  | tr -d '[:space:]')"
if [[ ! "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+ ]]; then
    echo "consumer-smoke: could not read a version from gradle.properties (got '$VERSION')" >&2
    exit 1
fi

echo "consumer-smoke: publishing com.monkopedia:{kcsg,kcsg-dsl}:$VERSION to $REPO_DIR"

# Start from nothing so a publish failure cannot be papered over by a previous run's artifacts.
rm -rf "$REPO_DIR"

# -Pkcsg.consumerSmoke makes the publication unsigned and points it at REPO_DIR. The JVM
# publications carry the variant metadata under test; the root (kotlinMultiplatform) modules
# are what the plain `com.monkopedia:kcsg-dsl` coordinate resolves to first.
"$ROOT/gradlew" -p "$ROOT" --console=plain -Pkcsg.consumerSmoke=true \
    :kcsg:publishJvmPublicationToConsumerSmokeRepository \
    :kcsg:publishKotlinMultiplatformPublicationToConsumerSmokeRepository \
    :kcsg-dsl:publishJvmPublicationToConsumerSmokeRepository \
    :kcsg-dsl:publishKotlinMultiplatformPublicationToConsumerSmokeRepository

# Positive control on the publish step itself: an empty repository would make the consumer
# build fail for a reason that has nothing to do with #51, and a red without a named cause is
# not evidence.
for module in kcsg kcsg-dsl; do
    for coordinate in "$module" "$module-jvm"; do
        expected="$REPO_DIR/com/monkopedia/$coordinate/$VERSION/$coordinate-$VERSION.module"
        if [[ ! -f "$expected" ]]; then
            echo "consumer-smoke: publish step did not produce $expected" >&2
            exit 1
        fi
    done
done
echo "consumer-smoke: 4 module descriptors published"

echo "consumer-smoke: compiling the standalone consumers"

# --rerun-tasks, and the outputs deleted first, because the jars are byte-identical between
# runs whenever the sources are: Gradle then reports `:dsl-only:compileKotlin UP-TO-DATE` and
# the whole assertion is skipped. Measured on a re-run after a genuine 10s compile:
# `BUILD SUCCESSFUL in 1s`, both compile tasks UP-TO-DATE. A green that a previous run earned
# is indistinguishable from one this run earned, so force the compile every time. It is two
# source files; the cost is a second.
rm -rf "$CONSUMER_DIR"/dsl-only/build "$CONSUMER_DIR"/core-only/build

set +e
"$ROOT/gradlew" -p "$CONSUMER_DIR" --console=plain --rerun-tasks \
    -PkcsgVersion="$VERSION" -PkcsgRepo="$REPO_DIR" \
    :dsl-only:compileKotlin :core-only:compileKotlin
status=$?
set -e

if [[ $status -ne 0 ]]; then
    cat >&2 <<'BANNER'

================================================================================
CONSUMER RESOLUTION SMOKE TEST FAILED

A build that depends on a published kcsg coordinate ALONE cannot compile against
its public API: types that appear in that API are missing from the consumer's
compile classpath.

Cause, if the errors above are unresolved references to com.monkopedia.kcsg.* or
kotlinx.io.*: the dependency that owns them is declared `implementation` instead
of `api` in kcsg/build.gradle.kts or kcsg-dsl/build.gradle.kts, so Gradle omits
it from the published jvmApiElements variant.

See https://github.com/Monkopedia/kcsg/issues/51
================================================================================
BANNER
    exit "$status"
fi

echo
echo "consumer-smoke: PASSED -- com.monkopedia:kcsg-dsl:$VERSION and com.monkopedia:kcsg:$VERSION"
echo "consumer-smoke: are each usable as a sole dependency on the JVM."
