-- Seed cell_tower_cache with OpenCellID data for MCC 505 (Australia) so the
-- demo has cache hits to explore without calling the Google Geolocation API.
-- Coordinates in the source CSV are WGS84 (EPSG:4326), matching
-- cell_tower_cache.location.

CREATE TEMP TABLE cell_tower_import (
    radio          TEXT,
    mcc            INTEGER,
    net            INTEGER,
    area           INTEGER,
    cell           BIGINT,
    unit           INTEGER,
    lon            DOUBLE PRECISION,
    lat            DOUBLE PRECISION,
    range          INTEGER,
    samples        INTEGER,
    changeable     INTEGER,
    created        BIGINT,
    updated        BIGINT,
    average_signal INTEGER
);

COPY cell_tower_import FROM '/seed-data/cell-towers/505.csv' WITH (FORMAT csv);

-- A handful of rows have cell IDs outside the INTEGER range used by
-- cell_tower_cache.cell_id; skip those rather than widening the schema.
-- `coverage` is a circle of radius `range` (metres) around the tower,
-- approximating the cell's footprint as reported by OpenCellID.
INSERT INTO cell_tower_cache (mcc, mnc, lac, cell_id, location, range_m, coverage, resolved_at)
SELECT DISTINCT ON (mcc, net, area, cell)
    mcc,
    net,
    area,
    cell,
    ST_SetSRID(ST_MakePoint(lon, lat), 4326)::geography,
    range,
    ST_Buffer(ST_SetSRID(ST_MakePoint(lon, lat), 4326)::geography, range),
    to_timestamp(updated)
FROM cell_tower_import
WHERE cell BETWEEN 0 AND 2147483647
ORDER BY mcc, net, area, cell, updated DESC
ON CONFLICT (mcc, mnc, lac, cell_id) DO NOTHING;
