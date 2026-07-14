#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
readonly RAW_DIR="${PROJECT_ROOT}/data/raw"

# EDRDG regenerates these distributions regularly and does not publish SHA-256
# sidecar files. These hashes pin the snapshots served on 2026-07-15 JST.
readonly KANJIDIC2_URL="https://www.edrdg.org/kanjidic/kanjidic2.xml.gz"
readonly KANJIDIC2_SHA256="219a52db91dcc0aaf509eb9aca67b91e5952b28e595f267a3857035d88c81c74"

readonly KRADFILE_URL="https://www.edrdg.org/pub/Nihongo/kradfile.gz"
readonly KRADFILE_SHA256="0c5487c1de77e36ffb5bde652f469f2de9c52efc8320137c115506f3500e9c5f"

readonly JMDICT_URL="https://www.edrdg.org/pub/Nihongo/JMdict_e.gz"
readonly JMDICT_SHA256="f78bba9d1ade4d7327bca7cfc9e9ba5b5f796f69eb7868358b98307f453c3989"

# KanjiVG's main archive is the upstream-recommended non-variant SVG set.
readonly KANJIVG_URL="https://github.com/KanjiVG/kanjivg/releases/download/r20250816/kanjivg-20250816-main.zip"
readonly KANJIVG_SHA256="69a2944ec1183086fdee5ba9c1f48bc306b867480a95b2f337f3203bf50689a3"

usage() {
  cat <<'EOF'
Usage: scripts/download-sources.sh

Downloads, verifies, and extracts the Phase 1 source data into data/raw/:
  data/raw/kanjidic2.xml
  data/raw/kradfile              (EUC-JP; intentionally not converted)
  data/raw/JMdict_e
  data/raw/kanjivg/*.svg

The script refuses to overwrite an existing source tree. If an EDRDG checksum
has changed, inspect the upstream change and deliberately update the pinned
hash in this script; never bypass verification.
EOF
}

if [[ "${1:-}" == "--help" || "${1:-}" == "-h" ]]; then
  usage
  exit 0
fi

if (( $# != 0 )); then
  usage >&2
  exit 2
fi

for command in curl gzip unzip find; do
  if ! command -v "${command}" >/dev/null 2>&1; then
    echo "Required command not found: ${command}" >&2
    exit 1
  fi
done

sha256_file() {
  local file="$1"

  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "${file}" | awk '{print $1}'
  elif command -v shasum >/dev/null 2>&1; then
    shasum -a 256 "${file}" | awk '{print $1}'
  else
    echo "Neither sha256sum nor shasum is installed." >&2
    return 1
  fi
}

download_and_verify() {
  local label="$1"
  local url="$2"
  local expected_sha256="$3"
  local destination="$4"
  local actual_sha256

  echo "Downloading ${label}"
  curl --fail --location --retry 3 --output "${destination}" "${url}"

  actual_sha256="$(sha256_file "${destination}")"
  if [[ "${actual_sha256}" != "${expected_sha256}" ]]; then
    cat >&2 <<EOF
Checksum mismatch for ${label}.
URL:      ${url}
Expected: ${expected_sha256}
Actual:   ${actual_sha256}

The upstream file may have been regenerated. Inspect the upstream change and
update the pinned checksum deliberately; this script will not accept it silently.
EOF
    return 1
  fi

  echo "Verified ${label}: ${actual_sha256}"
}

for destination in \
  "${RAW_DIR}/kanjidic2.xml" \
  "${RAW_DIR}/kradfile" \
  "${RAW_DIR}/JMdict_e" \
  "${RAW_DIR}/kanjivg"; do
  if [[ -e "${destination}" ]]; then
    echo "Refusing to overwrite existing source: ${destination}" >&2
    exit 1
  fi
done

readonly TEMP_DIR="$(mktemp -d "${TMPDIR:-/tmp}/kanji-discovery-sources.XXXXXX")"
trap 'rm -rf "${TEMP_DIR}"' EXIT

download_and_verify \
  "KANJIDIC2" \
  "${KANJIDIC2_URL}" \
  "${KANJIDIC2_SHA256}" \
  "${TEMP_DIR}/kanjidic2.xml.gz"
download_and_verify \
  "KRADFILE" \
  "${KRADFILE_URL}" \
  "${KRADFILE_SHA256}" \
  "${TEMP_DIR}/kradfile.gz"
download_and_verify \
  "JMdict (English)" \
  "${JMDICT_URL}" \
  "${JMDICT_SHA256}" \
  "${TEMP_DIR}/JMdict_e.gz"
download_and_verify \
  "KanjiVG r20250816 main SVG archive" \
  "${KANJIVG_URL}" \
  "${KANJIVG_SHA256}" \
  "${TEMP_DIR}/kanjivg.zip"

mkdir -p "${TEMP_DIR}/extracted" "${TEMP_DIR}/kanjivg-unpacked"
gzip --decompress --stdout "${TEMP_DIR}/kanjidic2.xml.gz" \
  > "${TEMP_DIR}/extracted/kanjidic2.xml"
gzip --decompress --stdout "${TEMP_DIR}/kradfile.gz" \
  > "${TEMP_DIR}/extracted/kradfile"
gzip --decompress --stdout "${TEMP_DIR}/JMdict_e.gz" \
  > "${TEMP_DIR}/extracted/JMdict_e"
unzip -q "${TEMP_DIR}/kanjivg.zip" -d "${TEMP_DIR}/kanjivg-unpacked"

readonly KANJIVG_SOURCE_DIR="$(
  find "${TEMP_DIR}/kanjivg-unpacked" -type d -name kanji -print -quit
)"
if [[ -z "${KANJIVG_SOURCE_DIR}" ]]; then
  echo "KanjiVG archive did not contain the expected kanji/ directory." >&2
  exit 1
fi

readonly SVG_COUNT="$(find "${KANJIVG_SOURCE_DIR}" -maxdepth 1 -type f -name '*.svg' | wc -l | tr -d ' ')"
if (( SVG_COUNT < 100 )); then
  echo "KanjiVG archive contained only ${SVG_COUNT} SVG files; refusing it." >&2
  exit 1
fi

mkdir -p "${RAW_DIR}" "${RAW_DIR}/.archives"
mv "${TEMP_DIR}/extracted/kanjidic2.xml" "${RAW_DIR}/kanjidic2.xml"
mv "${TEMP_DIR}/extracted/kradfile" "${RAW_DIR}/kradfile"
mv "${TEMP_DIR}/extracted/JMdict_e" "${RAW_DIR}/JMdict_e"
mv "${KANJIVG_SOURCE_DIR}" "${RAW_DIR}/kanjivg"
cp "${TEMP_DIR}/kanjidic2.xml.gz" "${RAW_DIR}/.archives/"
cp "${TEMP_DIR}/kradfile.gz" "${RAW_DIR}/.archives/"
cp "${TEMP_DIR}/JMdict_e.gz" "${RAW_DIR}/.archives/"
cp "${TEMP_DIR}/kanjivg.zip" "${RAW_DIR}/.archives/kanjivg-20250816-main.zip"

cat <<EOF
Source installation complete:
  ${RAW_DIR}/kanjidic2.xml
  ${RAW_DIR}/kradfile (EUC-JP)
  ${RAW_DIR}/JMdict_e
  ${RAW_DIR}/kanjivg (${SVG_COUNT} SVG files)

Verified source archives are retained in ${RAW_DIR}/.archives/.
EOF
