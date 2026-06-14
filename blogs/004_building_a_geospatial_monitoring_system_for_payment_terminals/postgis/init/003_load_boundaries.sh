#!/bin/bash
# Load the Australian SAL, world country, and populated-places shapefiles into
# PostGIS tables for GeoServer to publish, reprojecting everything to EPSG:4326.
set -euo pipefail

PSQL=(psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB")

# Suburbs and Localities (ABS ASGS, GDA2020 / EPSG:7844), simplified to keep the
# shapefile small and reprojected to EPSG:4326 -> tag as EPSG:4326
shp2pgsql -s 4326 -I -D \
    /seed-data/SAL_2021_AUST_GDA2020_SIMPLIFIED/SAL_2021_AUST_GDA2020_simplified.shp \
    public.sal_boundaries | "${PSQL[@]}"

# World country boundaries (Natural Earth, already WGS84) -> tag as EPSG:4326
shp2pgsql -s 4326 -I -D \
    /seed-data/ne_10m_admin_0_countries/ne_10m_admin_0_countries.shp \
    public.country_boundaries | "${PSQL[@]}"

# World populated places (Natural Earth, already WGS84) -> tag as EPSG:4326
shp2pgsql -s 4326 -I -D \
    /seed-data/ne_10m_populated_places/ne_10m_populated_places.shp \
    public.populated_places | "${PSQL[@]}"

# Flag Australian state/territory capitals so the populated_places style can
# show them at a wider zoom than other towns of similar population.
"${PSQL[@]}" <<'SQL'
ALTER TABLE populated_places ADD COLUMN is_capital boolean NOT NULL DEFAULT false;

UPDATE populated_places SET is_capital = true
WHERE adm0name = 'Australia'
  AND name IN ('Sydney', 'Melbourne', 'Brisbane', 'Perth', 'Adelaide', 'Hobart', 'Darwin', 'Canberra');
SQL

# Ocean/water backdrop for the base map: a single world-extent polygon drawn
# as the bottom-most layer in the demo-basemap layer group, so any area not
# covered by land layers renders as blue water.
"${PSQL[@]}" <<'SQL'
CREATE TABLE ocean_background (
    gid serial PRIMARY KEY,
    geom geometry(Polygon, 4326)
);

INSERT INTO ocean_background (geom) VALUES (
    ST_MakeEnvelope(-180, -90, 180, 90, 4326)
);

CREATE INDEX ocean_background_geom_idx ON ocean_background USING gist(geom);
SQL
