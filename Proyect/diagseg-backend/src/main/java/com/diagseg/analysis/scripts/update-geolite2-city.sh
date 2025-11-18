#!/usr/bin/env bash

set -euo pipefail

# Este archivo está en: src/main/java/com/diagseg/analysis/scripts
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Desde scripts: ../../../../../ -> src/main
DEST_DIR="${SCRIPT_DIR}/../../../../../resources/geo"
EDITION_ID="GeoLite2-City"

if [[ -z "${MAXMIND_LICENSE_KEY:-}" ]]; then
  echo "ERROR: La variable de entorno MAXMIND_LICENSE_KEY no está definida."
  echo "Crea un License Key en MaxMind y exporta, por ejemplo:"
  echo "  export MAXMIND_LICENSE_KEY=tu_licencia_aqui"
  exit 1
fi

echo "Actualizando base de datos ${EDITION_ID}..."
echo "SCRIPT_DIR: ${SCRIPT_DIR}"
echo "Destino: ${DEST_DIR}"

mkdir -p "${DEST_DIR}"

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

TAR_FILE="${TMP_DIR}/${EDITION_ID}.tar.gz"

DOWNLOAD_URL="https://download.maxmind.com/app/geoip_download?edition_id=${EDITION_ID}&license_key=${MAXMIND_LICENSE_KEY}&suffix=tar.gz"

echo "Descargando desde:"
echo "  ${DOWNLOAD_URL}"

curl -L -o "${TAR_FILE}" "${DOWNLOAD_URL}"

echo "Descomprimiendo..."
tar -xzf "${TAR_FILE}" -C "${TMP_DIR}"

MMDB_PATH="$(find "${TMP_DIR}" -name "${EDITION_ID}.mmdb" -type f | head -n 1)"

if [[ -z "${MMDB_PATH}" ]]; then
  echo "ERROR: No se encontró el archivo ${EDITION_ID}.mmdb en el tar descargado."
  exit 1
fi

echo "Copiando base de datos a ${DEST_DIR}/GeoLite2-City.mmdb"
cp "${MMDB_PATH}" "${DEST_DIR}/GeoLite2-City.mmdb"

echo "Actualización completada correctamente ✅"
