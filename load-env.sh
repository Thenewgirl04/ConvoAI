#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${ROOT_DIR}/.env"

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "Missing .env file. Copy example.env to .env and add your keys:"
  echo "  cp example.env .env"
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a

# Map Postgres vars to Spring Boot datasource env names
export SPRING_DATASOURCE_USERNAME="${POSTGRES_USER:-}"
export SPRING_DATASOURCE_PASSWORD="${POSTGRES_PASSWORD:-}"

echo "Loaded environment from .env"
