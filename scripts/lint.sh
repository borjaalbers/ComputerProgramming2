#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
APP_DIR="$PROJECT_ROOT/gymflow-app"

if ! command -v mvn &> /dev/null; then
  echo "Maven not found. Please install Maven (https://maven.apache.org/)." >&2
  exit 1
fi

cd "$APP_DIR"
mvn clean compile test
