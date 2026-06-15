-- Seed ~1000 demo devices (terminal-1001 .. terminal-2000) with randomized
-- telemetry, weighted toward major Australian cities, plus matching
-- connectivity history and a 30-day sales summary. Gives the demo support
-- pages (device list/detail, LWT simulator) and the sales heatmap query
-- enough data to be interesting out of the box.

-- Merchant category codes/descriptions, loaded from the full reference list in
-- src-data/mcc_codes.csv (mounted read-only at /seed-data). The `mcc` column is
-- a zero-padded 4-digit code in the CSV (e.g. "0742"); COPY into an INTEGER
-- column parses it as a number, dropping the leading zero.
CREATE TEMP TABLE merchant_category_import (
    mcc                 INTEGER,
    edited_description  TEXT,
    combined_description TEXT,
    usda_description    TEXT,
    irs_description     TEXT,
    irs_reportable      TEXT
);

COPY merchant_category_import FROM '/seed-data/mcc_codes.csv' WITH (FORMAT csv, HEADER true);

CREATE TEMP TABLE demo_devices AS
WITH australia AS (
    SELECT geom FROM country_boundaries WHERE name = 'Australia'
),
cities(name, lat, lon, weight, radius_km) AS (
    VALUES
        -- Capital/major cities: most devices, modest spread around each.
        ('Sydney', -33.8688, 151.2093, 27, 40),
        ('Melbourne', -37.8136, 144.9631, 26, 40),
        ('Brisbane', -27.4698, 153.0251, 13, 40),
        ('Perth', -31.9505, 115.8605, 11, 40),
        ('Adelaide', -34.9285, 138.6007, 7, 40),
        ('Gold Coast', -28.0167, 153.4000, 4, 30),
        ('Newcastle', -32.9283, 151.7817, 3, 30),
        ('Canberra', -35.2809, 149.1300, 2, 40),
        ('Wollongong', -34.4278, 150.8931, 2, 30),
        ('Hobart', -42.8821, 147.3272, 1, 30),
        ('Darwin', -12.4634, 130.8456, 1, 30),
        -- Regional/remote towns: smaller share, wider spread so devices reach
        -- further into regional and outback Australia rather than clustering
        -- around the capitals.
        ('Cairns', -16.9203, 145.7710, 2, 100),
        ('Townsville', -19.2590, 146.8169, 2, 100),
        ('Toowoomba', -27.5598, 151.9507, 1, 80),
        ('Bendigo', -36.7570, 144.2794, 1, 80),
        ('Albury', -36.0737, 146.9135, 1, 100),
        ('Dubbo', -32.2569, 148.6011, 1, 120),
        ('Broken Hill', -31.9580, 141.4664, 1, 150),
        ('Alice Springs', -23.6980, 133.8807, 1, 200),
        ('Mount Isa', -20.7256, 139.4927, 1, 200),
        ('Kalgoorlie', -30.7489, 121.4658, 1, 180),
        ('Port Hedland', -20.3104, 118.6011, 1, 150),
        ('Coober Pedy', -29.0135, 134.7544, 1, 250),
        ('Katherine', -14.4652, 132.2635, 1, 180)
),
cities_cum AS (
    SELECT
        name, lat, lon, radius_km,
        SUM(weight) OVER (ORDER BY name) - weight AS lo,
        SUM(weight) OVER (ORDER BY name)          AS hi,
        SUM(weight) OVER ()                       AS total
    FROM cities
)
SELECT
    'terminal-' || (1000 + n)                                      AS device_id,
    -- Random point within radius_km of the chosen city/town. sqrt(random())
    -- gives a uniform distribution over the surrounding area (not just
    -- radius). Up to 12 candidates are tried and the first that falls on
    -- Australian land (per country_boundaries) is kept, so coastal cities
    -- never place a device out in the ocean; if every candidate misses
    -- (vanishingly unlikely), fall back to the town's own coordinates.
    COALESCE(
        (
            SELECT candidate.geom
            FROM (
                SELECT
                    ST_Project(
                        ST_SetSRID(ST_MakePoint(city.lon, city.lat), 4326)::geography,
                        sqrt(random()) * city.radius_km * 1000,
                        random() * 2 * pi()
                    )::geometry AS geom
                FROM generate_series(1, 12)
            ) AS candidate, australia
            WHERE ST_Covers(australia.geom, candidate.geom)
            ORDER BY random()
            LIMIT 1
        ),
        ST_SetSRID(ST_MakePoint(city.lon, city.lat), 4326)
    )::geography                                                   AS location,
    cat.mcc                                                          AS merchant_category_code,
    cat.edited_description                                          AS merchant_category_name,
    rnd.avg_ticket_cents                                            AS avg_ticket_cents,
    floor(random() * 101)::smallint                                 AS battery_pct,
    floor(random() * 101)::smallint                                 AS memory_used_pct,
    -- Less than 3% ethernet; the remainder split 1/3 wifi, 2/3 mobile.
    CASE
        WHEN rnd.net_r < 0.97 / 3.0 THEN 'WIFI'
        WHEN rnd.net_r < 0.97 THEN 'MOBILE'
        ELSE 'ETHERNET'
    END                                                              AS network_interface,
    CASE
        WHEN rnd.prov_r < 0.45 THEN 'Telstra'
        WHEN rnd.prov_r < 0.75 THEN 'Optus'
        WHEN rnd.prov_r < 0.90 THEN 'Vodafone'
        ELSE 'TPG'
    END                                                              AS network_provider,
    '8961' || lpad(floor(random() * 1e15)::bigint::text, 15, '0')   AS sim1_id,
    '8961' || lpad(floor(random() * 1e15)::bigint::text, 15, '0')   AS sim2_id,
    (floor(random() * 2) + 1)::smallint                             AS active_sim_slot,
    CASE
        WHEN rnd.conn_r < 0.85 THEN 'online'
        WHEN rnd.conn_r < 0.95 THEN 'expected_offline'
        ELSE 'unexpected_offline'
    END                                                              AS connectivity_status,
    now() - (random() * interval '48 hours')                        AS connectivity_status_at,
    now() - (random() * interval '24 hours')                        AS last_seen
FROM generate_series(1, 1000) AS n
-- A single per-row LATERAL of scalar random() values. The `WHERE n IS NOT
-- NULL` reference to the outer `n` is required: an uncorrelated `LATERAL
-- (SELECT random())` subquery is hoisted by the planner and evaluated only
-- ONCE for the whole query, giving every row the same value.
CROSS JOIN LATERAL (
    SELECT
        random() * (SELECT total FROM cities_cum LIMIT 1) AS pick_city_r,
        random()                                          AS net_r,
        random()                                          AS prov_r,
        random()                                          AS conn_r,
        500 + floor(random() * 29500)                     AS avg_ticket_cents
    WHERE n IS NOT NULL
) AS rnd
CROSS JOIN LATERAL (
    SELECT name, lat, lon, radius_km FROM cities_cum WHERE rnd.pick_city_r >= lo AND rnd.pick_city_r < hi LIMIT 1
) AS city
-- Pick a merchant category uniformly at random from the full reference list,
-- so devices are spread across the ~980 available categories rather than a
-- small hand-picked set. `WHERE n IS NOT NULL` is the same per-row
-- correlation trick as above.
CROSS JOIN LATERAL (
    SELECT mcc, edited_description FROM merchant_category_import WHERE n IS NOT NULL ORDER BY random() LIMIT 1
) AS cat;

INSERT INTO device_locations (
    device_id, location, source, last_seen,
    merchant_category_code, merchant_category_name,
    battery_pct, memory_used_pct,
    network_interface, network_provider,
    sim1_id, sim2_id, active_sim_slot,
    connectivity_status, connectivity_status_at
)
SELECT
    device_id, location, 'seed', last_seen,
    merchant_category_code, merchant_category_name,
    battery_pct, memory_used_pct,
    network_interface, network_provider,
    sim1_id, sim2_id, active_sim_slot,
    connectivity_status, connectivity_status_at
FROM demo_devices
ON CONFLICT (device_id) DO NOTHING;

-- One seed connectivity event per device, matching its current status.
INSERT INTO device_connectivity_events (device_id, status, occurred_at)
SELECT device_id, connectivity_status, connectivity_status_at
FROM demo_devices;

-- 30 days of sales history per device (~30,000 rows), scaled by each
-- device's average ticket size.
INSERT INTO device_sales_summary (device_id, summary_date, transaction_count, sales_total_cents)
SELECT
    d.device_id,
    CURRENT_DATE - g.day,
    txn.count,
    (txn.count * d.avg_ticket_cents * (0.5 + random()))::bigint
FROM demo_devices d
CROSS JOIN generate_series(0, 29) AS g(day)
CROSS JOIN LATERAL (SELECT 5 + floor(random() * 60)::int AS count) AS txn
ON CONFLICT (device_id, summary_date) DO NOTHING;
