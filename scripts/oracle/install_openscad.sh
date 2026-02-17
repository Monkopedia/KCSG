#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd)"
INSTALL_ROOT="${OPENSCAD_INSTALL_DIR:-$REPO_ROOT/.tools/openscad}"
BASE_URL="${OPENSCAD_BASE_URL:-https://files.openscad.org/snapshots}"
ARTIFACT_NAME="${OPENSCAD_ARTIFACT_NAME:-OpenSCAD-2025.06.03.ai25586-x86_64.AppImage}"

if [[ "${1:-}" == "--help" || "${1:-}" == "-h" ]]; then
    cat <<'USAGE'
Usage: install_openscad.sh

Environment overrides:
  OPENSCAD_INSTALL_DIR   Install directory (default: .tools/openscad)
  OPENSCAD_BASE_URL      Download base URL (default: https://files.openscad.org/snapshots)
  OPENSCAD_ARTIFACT_NAME Artifact filename (default: OpenSCAD-2025.06.03.ai25586-x86_64.AppImage)
USAGE
    exit 0
fi

if [[ "$(uname -s)" != "Linux" ]]; then
    echo "OpenSCAD bootstrap currently supports Linux only." >&2
    exit 1
fi

if [[ "$(uname -m)" != "x86_64" ]]; then
    echo "OpenSCAD bootstrap currently supports x86_64 only." >&2
    exit 1
fi

if ! command -v sha256sum >/dev/null 2>&1; then
    echo "sha256sum is required to verify OpenSCAD downloads." >&2
    exit 1
fi

download_file() {
    local url="$1"
    local dest="$2"

    if command -v curl >/dev/null 2>&1; then
        curl -fL --retry 3 --retry-delay 2 -o "$dest" "$url"
        return
    fi

    if command -v wget >/dev/null 2>&1; then
        wget -O "$dest" "$url"
        return
    fi

    echo "Neither curl nor wget is available for downloads." >&2
    exit 1
}

mkdir -p "$INSTALL_ROOT"

artifact_url="$BASE_URL/$ARTIFACT_NAME"
sha_url="$artifact_url.sha256"
artifact_path="$INSTALL_ROOT/$ARTIFACT_NAME"
sha_path="$artifact_path.sha256"
wrapper_path="$INSTALL_ROOT/openscad"

if [[ ! -f "$artifact_path" ]]; then
    echo "Downloading $artifact_url"
    download_file "$artifact_url" "$artifact_path"
fi

if [[ ! -f "$sha_path" ]]; then
    echo "Downloading $sha_url"
    download_file "$sha_url" "$sha_path"
fi

(
    cd "$INSTALL_ROOT"
    sha256sum -c "$(basename "$sha_path")"
)

chmod +x "$artifact_path"

cat >"$wrapper_path" <<'WRAPPER'
#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
artifact_name="${OPENSCAD_ARTIFACT_NAME:-OpenSCAD-2025.06.03.ai25586-x86_64.AppImage}"
appimage_path="$script_dir/$artifact_name"

if [[ ! -x "$appimage_path" ]]; then
    echo "Missing OpenSCAD AppImage: $appimage_path" >&2
    exit 1
fi

# Many CI/container environments do not provide FUSE mounts.
APPIMAGE_EXTRACT_AND_RUN=1 exec "$appimage_path" "$@"
WRAPPER

chmod +x "$wrapper_path"

echo "OpenSCAD installed at $wrapper_path"
"$wrapper_path" --version
