CREATE EXTENSION IF NOT EXISTS postgis;

-- Last known location and device telemetry for each device
CREATE TABLE IF NOT EXISTS device_locations (
    device_id              TEXT PRIMARY KEY,
    location               GEOGRAPHY(Point, 4326) NOT NULL,
    source                 TEXT NOT NULL, -- 'cache' or 'google'
    last_seen              TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- Device/business telemetry. "merchant_category_*" is the merchant category
    -- code/name (e.g. coffee shop, clothing store) - distinct from the cell
    -- tower mobile country code (mcc) used elsewhere in this schema.
    merchant_category_code INTEGER,
    merchant_category_name TEXT,
    battery_pct            SMALLINT CHECK (battery_pct BETWEEN 0 AND 100),
    memory_used_pct        SMALLINT CHECK (memory_used_pct BETWEEN 0 AND 100),
    network_interface      TEXT CHECK (network_interface IN ('WIFI', 'MOBILE', 'ETHERNET')),
    network_provider       TEXT,
    sim1_id                TEXT,
    sim2_id                TEXT,
    active_sim_slot        SMALLINT CHECK (active_sim_slot IN (1, 2)),

    -- Current connectivity status, maintained alongside device_connectivity_events.
    connectivity_status    TEXT NOT NULL DEFAULT 'online'
        CHECK (connectivity_status IN ('online', 'expected_offline', 'unexpected_offline')),
    connectivity_status_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS device_locations_geo_idx
    ON device_locations USING GIST (location);

-- LWT-style connectivity audit log (online / expected_offline / unexpected_offline)
CREATE TABLE IF NOT EXISTS device_connectivity_events (
    id          BIGSERIAL PRIMARY KEY,
    device_id   TEXT NOT NULL REFERENCES device_locations(device_id),
    status      TEXT NOT NULL CHECK (status IN ('online', 'expected_offline', 'unexpected_offline')),
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS device_connectivity_events_device_idx
    ON device_connectivity_events (device_id, occurred_at DESC);

-- Daily sales volume summary per device, for heatmaps / geospatial aggregation
CREATE TABLE IF NOT EXISTS device_sales_summary (
    device_id         TEXT NOT NULL REFERENCES device_locations(device_id),
    summary_date      DATE NOT NULL,
    transaction_count INTEGER NOT NULL,
    sales_total_cents BIGINT NOT NULL,
    PRIMARY KEY (device_id, summary_date)
);

CREATE INDEX IF NOT EXISTS device_sales_summary_date_idx
    ON device_sales_summary (summary_date);

-- Cache of previously resolved cell tower / network signatures
CREATE TABLE IF NOT EXISTS cell_tower_cache (
    mcc          INTEGER NOT NULL,
    mnc          INTEGER NOT NULL,
    lac          INTEGER NOT NULL,
    cell_id      INTEGER NOT NULL,
    location     GEOGRAPHY(Point, 4326) NOT NULL,
    range_m      INTEGER,
    coverage     GEOGRAPHY(Polygon, 4326),
    resolved_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (mcc, mnc, lac, cell_id)
);

CREATE INDEX IF NOT EXISTS cell_tower_cache_geo_idx
    ON cell_tower_cache USING GIST (location);

CREATE INDEX IF NOT EXISTS cell_tower_cache_coverage_idx
    ON cell_tower_cache USING GIST (coverage);

-- Cache of previously resolved WiFi access points, keyed by BSSID (MAC address)
CREATE TABLE IF NOT EXISTS wifi_location_cache (
    bssid        TEXT PRIMARY KEY,
    location     GEOGRAPHY(Point, 4326) NOT NULL,
    range_m      INTEGER,
    coverage     GEOGRAPHY(Polygon, 4326),
    resolved_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS wifi_location_cache_geo_idx
    ON wifi_location_cache USING GIST (location);

CREATE INDEX IF NOT EXISTS wifi_location_cache_coverage_idx
    ON wifi_location_cache USING GIST (coverage);
