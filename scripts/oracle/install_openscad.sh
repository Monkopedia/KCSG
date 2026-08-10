#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd)"
INSTALL_ROOT="${OPENSCAD_INSTALL_DIR:-$REPO_ROOT/.tools/openscad}"
BASE_URL="${OPENSCAD_BASE_URL:-https://files.openscad.org/snapshots}"
ARTIFACT_NAME="${OPENSCAD_ARTIFACT_NAME:-OpenSCAD-2026.01.02.ai30348-x86_64.AppImage}"

if [[ "${1:-}" == "--help" || "${1:-}" == "-h" ]]; then
    cat <<'USAGE'
Usage: install_openscad.sh

Environment overrides:
  OPENSCAD_INSTALL_DIR   Install directory (default: .tools/openscad)
  OPENSCAD_BASE_URL      Download base URL (default: https://files.openscad.org/snapshots)
  OPENSCAD_ARTIFACT_NAME Artifact filename (default: OpenSCAD-2026.01.02.ai30348-x86_64.AppImage)
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

# Same coreutils package as sha256sum above, so this costs nothing extra to demand, and it
# means there is no code path here that runs OpenSCAD without a bound.
if ! command -v timeout >/dev/null 2>&1; then
    echo "timeout is required so the install-time OpenSCAD check cannot hang." >&2
    exit 1
fi

# Bounds on a single download attempt. The AppImage is 84.6 MB and fetches in 4.3s here
# (~19 MB/s), so a 300s cap per attempt is ~70x the observed download and still only needs
# ~280 kB/s sustained -- far below anything a CI runner or a home connection provides. With
# --retry 3 the worst case is ~15 minutes instead of the unbounded wait a stalled connection
# currently produces, where --retry alone reconnects forever rather than failing.
DOWNLOAD_CONNECT_TIMEOUT_SECONDS=30
DOWNLOAD_MAX_TIME_SECONDS=300

download_file() {
    local url="$1"
    local dest="$2"

    if command -v curl >/dev/null 2>&1; then
        curl -fL \
            --connect-timeout "$DOWNLOAD_CONNECT_TIMEOUT_SECONDS" \
            --max-time "$DOWNLOAD_MAX_TIME_SECONDS" \
            --retry 3 --retry-delay 2 \
            -o "$dest" "$url"
        return
    fi

    if command -v wget >/dev/null 2>&1; then
        # wget has no whole-transfer cap; --timeout bounds connect/read/DNS stalls, which is
        # the failure mode --max-time addresses above, and --tries replaces wget's default 20.
        wget \
            --timeout="$DOWNLOAD_CONNECT_TIMEOUT_SECONDS" \
            --tries=3 \
            -O "$dest" "$url"
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
artifact_name="${OPENSCAD_ARTIFACT_NAME:-OpenSCAD-2026.01.02.ai30348-x86_64.AppImage}"
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

# The install-time smoke check is an OpenSCAD invocation like any other and can wedge the
# same way (this one runs before the generator's own budget starts), so it gets the same
# 120s bound the generator and the oracle test use.
VERIFY_TIMEOUT_SECONDS=120
verify_status=0
timeout --kill-after=10s "${VERIFY_TIMEOUT_SECONDS}s" "$wrapper_path" --version || verify_status=$?

if (( verify_status == 124 || verify_status == 137 )); then
    echo "Installed OpenSCAD did not respond to --version within ${VERIFY_TIMEOUT_SECONDS}s." >&2
    echo "The AppImage at $artifact_path is unusable; not leaving it in place." >&2
    rm -f "$wrapper_path"
    exit 124
fi

if (( verify_status != 0 )); then
    echo "Installed OpenSCAD failed --version with exit code $verify_status." >&2
    rm -f "$wrapper_path"
    exit "$verify_status"
fi
