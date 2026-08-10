#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd)"
SCAD_SOURCE="$REPO_ROOT/oracle/scad/boolean_oracles.scad"
INSTALL_SCRIPT="$REPO_ROOT/scripts/oracle/install_openscad.sh"
OPENSCAD_BIN="${OPENSCAD_BIN:-$REPO_ROOT/.tools/openscad/openscad}"
OUTPUT_DIR="$REPO_ROOT/kcsg/build/oracle-fixtures"
BACKENDS=("cgal" "manifold")

# Upper bound on a single OpenSCAD invocation. The slowest render in this fixture set is
# many_reductions_union under the cgal backend, measured at 3.7-4.0s locally and 5.45s on
# the runner that sized the matching bound in the oracle test (see #49/#55). 120s keeps the
# same value that test uses, which is ~20-30x the slowest known render, so it cannot become
# flaky on a slow machine or during cold AppImage extraction while still turning a wedged
# renderer into a named failure in two minutes.
RENDER_TIMEOUT_SECONDS="${OPENSCAD_RENDER_TIMEOUT_SECONDS:-120}"

# Upper bound on all OpenSCAD work in one run (the version probe plus every render). A full
# 2-backend, 12-scenario generation measures 8.6-8.8s wall clock, so 600s is ~70x the whole
# observed run. It matters because the realistic wedge -- a broken AppImage, a missing FUSE
# mount, a renderer that never returns -- wedges *every* invocation, and 24 renders each
# burning the 120s per-render bound would be 48 minutes of runner time. This caps that at 10
# minutes while staying 5x the per-render bound, so a single legitimately slow render cannot
# trip it.
RUN_TIMEOUT_SECONDS="${OPENSCAD_RUN_TIMEOUT_SECONDS:-600}"

# Grace period between SIGTERM and SIGKILL for a render that ignores the polite signal.
KILL_GRACE_SECONDS=10

usage() {
    cat <<'USAGE'
Usage: generate_openscad_oracles.sh [--output-dir <path>] [--backends <csv>]

Environment overrides:
  OPENSCAD_BIN                       OpenSCAD binary (default: .tools/openscad/openscad)
  OPENSCAD_RENDER_TIMEOUT_SECONDS    Per-render timeout (default: 120)
  OPENSCAD_RUN_TIMEOUT_SECONDS       Total budget for all renders (default: 600)

Examples:
  ./scripts/oracle/generate_openscad_oracles.sh
  ./scripts/oracle/generate_openscad_oracles.sh --output-dir /tmp/oracles
  ./scripts/oracle/generate_openscad_oracles.sh --backends cgal
USAGE
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --output-dir)
            OUTPUT_DIR="$2"
            shift 2
            ;;
        --backends)
            IFS=',' read -r -a BACKENDS <<<"$2"
            shift 2
            ;;
        --help|-h)
            usage
            exit 0
            ;;
        *)
            echo "Unknown argument: $1" >&2
            usage >&2
            exit 1
            ;;
    esac
done

if [[ ! -f "$SCAD_SOURCE" ]]; then
    echo "Missing scenario source: $SCAD_SOURCE" >&2
    exit 1
fi

if [[ ! -x "$OPENSCAD_BIN" ]]; then
    "$INSTALL_SCRIPT"
fi

TIMEOUT_BIN=""
for candidate in timeout gtimeout; do
    if command -v "$candidate" >/dev/null 2>&1; then
        TIMEOUT_BIN="$candidate"
        break
    fi
done

if [[ -z "$TIMEOUT_BIN" ]]; then
    echo "Neither 'timeout' nor 'gtimeout' is available." >&2
    echo "Oracle generation refuses to run unbounded; install GNU coreutils." >&2
    exit 1
fi

# Deadline for all OpenSCAD work, started after any bootstrap download so the render budget
# is spent on rendering rather than on installing.
RUN_DEADLINE=$(( $(date +%s) + RUN_TIMEOUT_SECONDS ))

# Deletes a partially written fixture so a failed render cannot leave one on disk.
discard_artifact() {
    if [[ -n "$1" ]]; then
        rm -f "$1"
    fi
}

# Runs one bounded OpenSCAD invocation.
#
#   run_openscad <label> <artifact-or-empty> <openscad args...>
#
# The effective bound is the smaller of the per-render timeout and whatever is left of the
# whole-run budget, so both limits are enforced by the same call. Any failure -- timeout,
# non-zero exit, or a missing output file -- aborts the whole run non-zero after deleting
# <artifact>, so a truncated STL can never be left behind for the oracle tests to validate
# against and pass.
run_openscad() {
    local label="$1"
    local artifact="$2"
    shift 2

    local remaining=$(( RUN_DEADLINE - $(date +%s) ))
    if (( remaining <= 0 )); then
        echo "Oracle generation exceeded its ${RUN_TIMEOUT_SECONDS}s total OpenSCAD budget" >&2
        echo "before starting $label. No fixture was written for it." >&2
        exit 124
    fi

    local budget="$RENDER_TIMEOUT_SECONDS"
    if (( remaining < budget )); then
        budget="$remaining"
    fi

    local status=0
    "$TIMEOUT_BIN" --kill-after="${KILL_GRACE_SECONDS}s" "${budget}s" \
        "$OPENSCAD_BIN" "$@" || status=$?

    if (( status == 124 || status == 137 )); then
        discard_artifact "$artifact"
        echo "OpenSCAD timed out after ${budget}s and was killed while running $label." >&2
        echo "Per-render bound ${RENDER_TIMEOUT_SECONDS}s, whole-run bound ${RUN_TIMEOUT_SECONDS}s." >&2
        exit 124
    fi

    if (( status != 0 )); then
        discard_artifact "$artifact"
        echo "OpenSCAD failed with exit code $status while running $label." >&2
        exit "$status"
    fi

    if [[ -n "$artifact" && ! -f "$artifact" ]]; then
        echo "OpenSCAD reported success but produced no output for $label ($artifact)." >&2
        exit 1
    fi
}

declare -a SCENARIOS=(
    "disjoint_union"
    "overlap_intersection"
    "containment_difference"
    "face_tangent_union"
    "edge_tangent_union"
    "vertex_tangent_union"
    "offset_cylinder_union"
    "many_reductions_union"
    "transformed_union_chain"
    "hull_tripod"
    "extrude_profile_difference"
    "mirrored_intersection"
)

rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

echo "Using OpenSCAD binary: $OPENSCAD_BIN"
# The version probe pays the same AppImage extraction cost as a render and can wedge the
# same way, so it gets the same bound rather than a second number nobody can justify.
run_openscad "the OpenSCAD version probe" "" --version

for backend in "${BACKENDS[@]}"; do
    backend="${backend,,}"
    case "$backend" in
        cgal|manifold)
            ;;
        *)
            echo "Unsupported backend: $backend (expected cgal/manifold)" >&2
            exit 1
            ;;
    esac

    backend_dir="$OUTPUT_DIR/$backend"
    mkdir -p "$backend_dir"

    for scenario in "${SCENARIOS[@]}"; do
        output_file="$backend_dir/$scenario.stl"
        echo "Rendering backend=$backend scenario=$scenario -> $output_file"
        run_openscad "backend=$backend scenario=$scenario" "$output_file" \
            --backend="$backend" \
            -D "scenario=\"$scenario\"" \
            -o "$output_file" \
            "$SCAD_SOURCE"
    done
done

{
    echo "scenario"
    for scenario in "${SCENARIOS[@]}"; do
        echo "$scenario"
    done
} > "$OUTPUT_DIR/scenarios.csv"

{
    echo "backend"
    for backend in "${BACKENDS[@]}"; do
        echo "${backend,,}"
    done
} > "$OUTPUT_DIR/backends.csv"

echo "Generated oracle fixtures in $OUTPUT_DIR"
