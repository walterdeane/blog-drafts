#!/bin/bash
# Load the Australian SAL and world country boundary shapefiles into PostGIS
# tables for GeoServer to publish, reprojecting everything to EPSG:4326.
set -euo pipefail

PSQL=(psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB")

# Suburbs and Localities (ABS ASGS, GDA2020 / EPSG:7844) -> EPSG:4326
shp2pgsql -s 7844:4326 -I -D \
    /seed-data/SAL_2021_AUST_GDA2020_SHP/SAL_2021_AUST_GDA2020.shp \
    public.sal_boundaries | "${PSQL[@]}"

# World country boundaries (Natural Earth, already WGS84) -> tag as EPSG:4326
shp2pgsql -s 4326 -I -D \
    /seed-data/ne_10m_admin_0_countries/ne_10m_admin_0_countries.shp \
    public.country_boundaries | "${PSQL[@]}"
