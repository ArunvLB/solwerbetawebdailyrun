#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

mkdir -p reports screenshots recordings

MVN_CMD="${MVN_CMD:-mvn}"
SUITE_XML_FILE="${SUITE_XML_FILE:-src/test/resources/testng.xml}"
TEST_ENV="${TEST_ENV:-QA}"
BROWSER="${BROWSER:-chrome}"
HEADLESS="${HEADLESS:-true}"
CI_MODE="${CI_MODE:-true}"
VIDEO_RECORDING_ENABLED="${VIDEO_RECORDING_ENABLED:-false}"

ARGS=(
  -B
  clean
  test
  "-DsuiteXmlFile=${SUITE_XML_FILE}"
  "-Denv=${TEST_ENV}"
  "-Dbrowser=${BROWSER}"
  "-Dheadless=${HEADLESS}"
  "-Dci=${CI_MODE}"
  "-Dvideo.recording.enabled=${VIDEO_RECORDING_ENABLED}"
)

if [[ -n "${BROWSER_BINARY_PATH:-}" ]]; then
  ARGS+=("-Dbrowser.binary.path=${BROWSER_BINARY_PATH}")
fi

"${MVN_CMD}" "${ARGS[@]}"
