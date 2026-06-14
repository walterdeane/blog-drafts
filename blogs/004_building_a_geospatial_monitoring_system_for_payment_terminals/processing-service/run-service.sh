#!/usr/bin/env bash
# Runs the processing service locally with bootRun.
#
# All configuration lives here in one place - edit the defaults below, or override any
# of them by exporting a value in your shell before running this script, e.g.:
#
#   GEOLOCATION_PROVIDER=google GOOGLE_GEOLOCATION_API_KEY=... ./run-service.sh
set -euo pipefail

# PostGIS connection (defaults match docker-compose.yml; run `docker compose up -d postgis`
# from the repo root first if you don't have your own instance)
export POSTGIS_HOST="${POSTGIS_HOST:-localhost}"
export POSTGIS_PORT="${POSTGIS_PORT:-5432}"
export POSTGIS_DB="${POSTGIS_DB:-geospatial}"
export POSTGIS_USER="${POSTGIS_USER:-geospatial}"
export POSTGIS_PASSWORD="${POSTGIS_PASSWORD:-geospatial}"

# Geolocation provider for the cell-tower fallback: "stub" (default, no external
# dependencies) or "google" (requires GOOGLE_GEOLOCATION_API_KEY)
export GEOLOCATION_PROVIDER="${GEOLOCATION_PROVIDER:-stub}"
export GOOGLE_GEOLOCATION_API_KEY="${GOOGLE_GEOLOCATION_API_KEY:-}"

# HTTP port (default 8081, avoids clashing with GeoServer on 8080)
export SERVER_PORT="${SERVER_PORT:-8081}"

cd "$(dirname "$0")"
./gradlew bootRun
