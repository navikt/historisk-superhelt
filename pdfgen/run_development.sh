#!/bin/bash

set -euo pipefail

CURRENT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export PDFGEN_PORT="8090"
docker compose -f "$CURRENT_PATH/compose.yml" up --build --watch
