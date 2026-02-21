#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd)"
SCAD_SOURCE="$REPO_ROOT/oracle/scad/boolean_oracles.scad"
INSTALL_SCRIPT="$REPO_ROOT/scripts/oracle/install_openscad.sh"
OPENSCAD_BIN="${OPENSCAD_BIN:-$REPO_ROOT/.tools/openscad/openscad}"
OUTPUT_DIR="$REPO_ROOT/kcsg/build/oracle-fixtures"
BACKENDS=("cgal" "manifold")

usage() {
    cat <<'USAGE'
Usage: generate_openscad_oracles.sh [--output-dir <path>] [--backends <csv>]

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
"$OPENSCAD_BIN" --version

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
        "$OPENSCAD_BIN" \
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
