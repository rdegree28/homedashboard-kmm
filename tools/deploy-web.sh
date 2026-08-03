#!/usr/bin/env bash
# Build the wasmJs web bundle and deploy it to the Caddy LXC on Proxmox.
#
# Usage:
#   tools/deploy-web.sh root@<CT-IP> [remote-dir]
#   tools/deploy-web.sh                      # uses env defaults below
#
# Env defaults (optional):
#   HOMEDASH_WEB_TARGET=root@192.168.1.50    # ssh target of the LXC
#   HOMEDASH_WEB_DIR=/var/www/homedash       # web root inside the LXC
#
# Note: ha.url/ha.token are baked in from local.properties at build time, so make sure
# those point at HA's LAN address before deploying.
set -euo pipefail

TARGET="${1:-${HOMEDASH_WEB_TARGET:-root@CT-IP}}"
REMOTE_DIR="${2:-${HOMEDASH_WEB_DIR:-/var/www/homedash}}"

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST="$REPO_ROOT/composeApp/build/dist/wasmJs/productionExecutable"

if [[ "$TARGET" == *CT-IP* ]]; then
  echo "Set the deploy target, e.g.:" >&2
  echo "  tools/deploy-web.sh root@192.168.1.50" >&2
  echo "  (or export HOMEDASH_WEB_TARGET=root@<CT-IP>)" >&2
  exit 1
fi

echo "==> Building wasmJs production distribution"
"$REPO_ROOT/gradlew" -p "$REPO_ROOT" :composeApp:wasmJsBrowserDistribution

# Stamp the build so already-open browser tabs can notice a new deploy and offer to reload.
# Written here rather than at build time so it changes on every deploy by construction.
date -u +%Y%m%dT%H%M%SZ > "$DIST/version.txt"
echo "==> Build stamp: $(cat "$DIST/version.txt")"

echo "==> Deploying $DIST/ -> $TARGET:$REMOTE_DIR/"
rsync -av --delete "$DIST/" "$TARGET:$REMOTE_DIR/"

echo "==> Done. Hard-refresh the browser (Cmd/Ctrl+Shift+R) to bypass cache."
