#!/usr/bin/env bash
# Build the wasmJs web bundle and deploy it to the Caddy LXC on Proxmox, then publish the
# matching debug APK to the Samba share so phones can sideload the same build.
#
# Usage:
#   tools/deploy-web.sh root@<CT-IP> [remote-dir]
#   tools/deploy-web.sh                      # uses env defaults below
#   tools/deploy-web.sh --no-apk             # web only, skip the APK step
#
# Env defaults (optional):
#   HOMEDASH_WEB_TARGET=root@192.168.1.50    # ssh target of the LXC
#   HOMEDASH_WEB_DIR=/var/www/homedash       # web root inside the LXC
#
# The APK step reads its settings from local.properties (gitignored, same file as ha.token):
#   smb.host=10.2.3.1                        # Samba server
#   smb.share=software                       # share name
#   smb.dir=android_apks/homedash            # optional subdirectory within the share
#   smb.user=claude                          # Samba username
#   smb.password=...                         # optional; omitted means Keychain/prompt
# With smb.host unset the APK step is skipped with a notice, so the web deploy still works.
#
# If the share is already mounted (Finder, say) that mount is reused as-is. Otherwise it is
# mounted to a temp point and unmounted afterwards, taking the password from the Keychain —
# which requires having mounted it once in Finder with "remember this password in my keychain".
#
# Note: ha.url/ha.token are baked in from local.properties at build time, so make sure
# those point at HA's LAN address before deploying.
set -euo pipefail

WITH_APK=1
ARGS=()
for arg in "$@"; do
  case "$arg" in
    --no-apk) WITH_APK=0 ;;
    *) ARGS+=("$arg") ;;
  esac
done

TARGET="${ARGS[0]:-${HOMEDASH_WEB_TARGET:-root@CT-IP}}"
REMOTE_DIR="${ARGS[1]:-${HOMEDASH_WEB_DIR:-/var/www/homedash}}"

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST="$REPO_ROOT/composeApp/build/dist/wasmJs/productionExecutable"
LOCAL_PROPS="$REPO_ROOT/local.properties"

if [[ "$TARGET" == *CT-IP* ]]; then
  echo "Set the deploy target, e.g.:" >&2
  echo "  tools/deploy-web.sh root@192.168.1.50" >&2
  echo "  (or export HOMEDASH_WEB_TARGET=root@<CT-IP>)" >&2
  exit 1
fi

# Read a key from local.properties, tolerating whitespace around the '='. Values may contain
# '=' (tokens, passwords) so only the first one splits.
prop() {
  [[ -f "$LOCAL_PROPS" ]] || return 0
  sed -n "s/^[[:space:]]*$1[[:space:]]*=[[:space:]]*\(.*\)/\1/p" "$LOCAL_PROPS" | head -1
}

echo "==> Building wasmJs production distribution"
"$REPO_ROOT/gradlew" -p "$REPO_ROOT" :composeApp:wasmJsBrowserDistribution

# Stamp the build so already-open browser tabs can notice a new deploy and offer to reload.
# Written here rather than at build time so it changes on every deploy by construction.
date -u +%Y%m%dT%H%M%SZ > "$DIST/version.txt"
echo "==> Build stamp: $(cat "$DIST/version.txt")"

echo "==> Deploying $DIST/ -> $TARGET:$REMOTE_DIR/"
rsync -av --delete "$DIST/" "$TARGET:$REMOTE_DIR/"

echo "==> Done. Hard-refresh the browser (Cmd/Ctrl+Shift+R) to bypass cache."

# ---------------------------------------------------------------------------
# APK -> Samba
#
# The debug APK, not release: release has no signingConfig, so assembleRelease produces an
# unsigned APK Android refuses to install. Debug is debug-signed and sideloads fine.
# ---------------------------------------------------------------------------
[[ "$WITH_APK" == 1 ]] || exit 0

SMB_HOST="$(prop 'smb\.host')"
if [[ -z "$SMB_HOST" ]]; then
  echo "==> Skipping APK publish: smb.host not set in local.properties (see header)."
  exit 0
fi

SMB_SHARE="$(prop 'smb\.share')"
SMB_DIR="$(prop 'smb\.dir')"
SMB_USER="$(prop 'smb\.user')"
SMB_PASSWORD="$(prop 'smb\.password')"

if [[ -z "$SMB_SHARE" || -z "$SMB_USER" ]]; then
  echo "APK publish needs smb.share and smb.user in local.properties (smb.host is set)." >&2
  exit 1
fi

# appVersionName is the single source of truth for the filename (versionCode is derived from it);
# AppInfo.VERSION is kept in step with it, so a mismatch here means the two drifted apart.
VERSION="$(sed -n 's/^[[:space:]]*val appVersionName[[:space:]]*=[[:space:]]*"\([^"]*\)".*/\1/p' \
  "$REPO_ROOT/composeApp/build.gradle.kts" | head -1)"
APP_VERSION="$(sed -n 's/.*val VERSION[[:space:]]*=[[:space:]]*"\([^"]*\)".*/\1/p' \
  "$REPO_ROOT/composeApp/src/commonMain/kotlin/com/degree/homedash/AppInfo.kt" | head -1)"
if [[ -z "$VERSION" ]]; then
  echo "Could not read versionName from composeApp/build.gradle.kts" >&2
  exit 1
fi
if [[ "$VERSION" != "$APP_VERSION" ]]; then
  echo "Version mismatch: build.gradle.kts says '$VERSION', AppInfo.VERSION says '$APP_VERSION'." >&2
  echo "Bring them back in step before publishing an APK." >&2
  exit 1
fi

echo "==> Building debug APK (v$VERSION)"
"$REPO_ROOT/gradlew" -p "$REPO_ROOT" :composeApp:assembleDebug

APK="$REPO_ROOT/composeApp/build/outputs/apk/debug/composeApp-debug.apk"
[[ -f "$APK" ]] || { echo "Expected APK not found at $APK" >&2; exit 1; }

# Reuse an existing mount if the share is already attached (mounting it a second time fails
# with "File exists"). Finder-mounted shares are the common case here, and a mount we did not
# make is not ours to tear down — OWN_MOUNT tracks that.
existing_mountpoint() {
  # `mount` lines look like: //user@host/share on /Volumes/share (smbfs, nodev, nosuid, ...)
  mount | awk -v host="$1" -v share="$2" '
    $0 ~ /\(smbfs/ {
      split($1, u, "@")
      if (tolower(u[2]) == tolower(host "/" share)) {
        # Everything between " on " and the trailing " (smbfs...)" is the mountpoint.
        line = $0
        sub(/^.* on /, "", line)
        sub(/ \(smbfs.*$/, "", line)
        print line
        exit
      }
    }'
}

MOUNTPOINT="$(existing_mountpoint "$SMB_HOST" "$SMB_SHARE")"
OWN_MOUNT=0

cleanup() {
  # Only unmount what this script mounted; leave a pre-existing Finder mount alone.
  if [[ "$OWN_MOUNT" == 1 ]]; then
    if mount | grep -q " on $MOUNTPOINT "; then
      umount "$MOUNTPOINT" 2>/dev/null || diskutil unmount force "$MOUNTPOINT" >/dev/null 2>&1 || true
    fi
    rmdir "$MOUNTPOINT" 2>/dev/null || true
  fi
}
trap cleanup EXIT

if [[ -n "$MOUNTPOINT" ]]; then
  echo "==> Using existing mount of //$SMB_HOST/$SMB_SHARE at $MOUNTPOINT"
else
  # URL-encode the credentials: an '@' or '/' in a password would otherwise break the smb:// URL.
  urlencode() {
    local s="$1" out="" c
    for (( i = 0; i < ${#s}; i++ )); do
      c="${s:i:1}"
      case "$c" in
        [a-zA-Z0-9.~_-]) out+="$c" ;;
        *) out+="$(printf '%%%02X' "'$c")" ;;
      esac
    done
    printf '%s' "$out"
  }

  SMB_URL="//$(urlencode "$SMB_USER")"
  if [[ -n "$SMB_PASSWORD" ]]; then
    SMB_URL+=":$(urlencode "$SMB_PASSWORD")"
  fi
  SMB_URL+="@$SMB_HOST/$SMB_SHARE"

  MOUNTPOINT="$(mktemp -d /tmp/homedash-smb.XXXXXX)"
  OWN_MOUNT=1

  echo "==> Mounting //$SMB_USER@$SMB_HOST/$SMB_SHARE"
  # -N first so an unattended run fails fast instead of blocking on a password prompt. It
  # succeeds when the Keychain holds this user's password (mount once in Finder with "remember
  # password" to put it there). Falling back to an interactive prompt only makes sense on a TTY.
  if ! mount_smbfs -N "$SMB_URL" "$MOUNTPOINT" 2>/dev/null; then
    if [[ -t 0 && -z "$SMB_PASSWORD" ]]; then
      echo "    No saved password for $SMB_USER@$SMB_HOST — prompting."
      if ! mount_smbfs "$SMB_URL" "$MOUNTPOINT"; then
        echo "Could not mount //$SMB_USER@$SMB_HOST/$SMB_SHARE" >&2
        exit 1
      fi
    else
      echo "Could not mount //$SMB_USER@$SMB_HOST/$SMB_SHARE" >&2
      echo "No password in the Keychain for '$SMB_USER'. Either mount the share once in Finder" >&2
      echo "with \"remember this password in my keychain\", or set smb.password in local.properties." >&2
      exit 1
    fi
  fi
fi

DEST="$MOUNTPOINT"
if [[ -n "$SMB_DIR" ]]; then
  DEST="$MOUNTPOINT/$SMB_DIR"
  mkdir -p "$DEST"
fi

# Versioned copy for history, plus a stable -latest name to bookmark on the phone.
echo "==> Publishing homedash-$VERSION.apk (+ homedash-latest.apk) to $SMB_SHARE/${SMB_DIR:-}"
cp "$APK" "$DEST/homedash-$VERSION.apk"
cp "$APK" "$DEST/homedash-latest.apk"
ls -l "$DEST/homedash-$VERSION.apk" "$DEST/homedash-latest.apk"

echo "==> APK published."
