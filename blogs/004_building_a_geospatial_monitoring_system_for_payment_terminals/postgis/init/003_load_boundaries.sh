#!/bin/bash
# Load the Australian LGA and world country boundary shapefiles into PostGIS
# tables for GeoServer to publish, reprojecting everything to EPSG:4326.
set -euo pipefail

PSQL=(psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB")

# Local Government Areas (ABS ASGS, GDA2020 / EPSG:7844) -> EPSG:4326
shp2pgsql -s 7844:4326 -I -D \
    /seed-data/LGA_2025_AUST_GDA2020/LGA_2025_AUST_GDA2020.shp \
    public.lga_boundaries | "${PSQL[@]}"

# World country boundaries (Natural Earth, already WGS84) -> tag as EPSG:4326
shp2pgsql -s 4326 -I -D \
    /seed-data/ne_10m_admin_0_countries/ne_10m_admin_0_countries.shp \
    public.country_boundaries | "${PSQL[@]}"
