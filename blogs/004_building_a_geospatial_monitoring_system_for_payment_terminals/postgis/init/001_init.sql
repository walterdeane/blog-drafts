CREATE EXTENSION IF NOT EXISTS postgis;

-- Last known location for each device
CREATE TABLE IF NOT EXISTS device_locations (
    device_id    TEXT PRIMARY KEY,
    location     GEOGRAPHY(Point, 4326) NOT NULL,
    source       TEXT NOT NULL, -- 'cache' or 'google'
    last_seen    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS device_locations_geo_idx
    ON device_locations USING GIST (location);

-- Cache of previously resolved cell tower / network signatures
CREATE TABLE IF NOT EXISTS cell_tower_cache (
    mcc          INTEGER NOT NULL,
    mnc          INTEGER NOT NULL,
    lac          INTEGER NOT NULL,
    cell_id      INTEGER NOT NULL,
    location     GEOGRAPHY(Point, 4326) NOT NULL,
    resolved_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (mcc, mnc, lac, cell_id)
);

CREATE INDEX IF NOT EXISTS cell_tower_cache_geo_idx
    ON cell_tower_cache USING GIST (location);
