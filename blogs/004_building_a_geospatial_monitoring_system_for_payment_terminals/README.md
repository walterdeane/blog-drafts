# Building a Geospatial Monitoring System for Payment Terminals

Proof-of-concept resources accompanying the blog post on building a geospatial monitoring
system for payment terminals (and other connected devices).

The full proposed architecture covers AWS IoT ingestion, a telemetry processing service,
a location cache, Aurora PostgreSQL + PostGIS, and GeoServer/OpenLayers for mapping. This
PoC focuses on the parts that can run locally:

- **PostGIS** - spatial storage for device locations, the cell tower cache, and the WiFi
  access point cache
- **GeoServer** - map/feature services on top of PostGIS
- **processing-service** - a minimal Kotlin/Spring Boot stand-in for the telemetry
  processing service, demonstrating the full cache-first location resolution strategy from
  the post: device GPS → cell tower cache → WiFi AP cache → external geolocation API
## Quick Start

### Start everything

```bash
docker compose up -d
```

This brings up PostGIS, GeoServer, and the processing service together:

- PostGIS is available on `localhost:5432` (db `geospatial`, user/password `geospatial`)
- GeoServer admin UI is available at [http://localhost:8080/geoserver](http://localhost:8080/geoserver)
  (user `admin`, password `geoserver` — these are the GeoServer defaults, left
  unchanged and committed as part of `geoserver/data/security/` since this PoC is
  for local/demo use only; a real deployment must change the admin credentials and
  master password before being exposed beyond localhost)
- The processing service is available on `localhost:8081` - see
  [processing-service/README.md](processing-service/README.md) for its API
- A home page at [http://localhost:8081](http://localhost:8081) links to both the
  support pages and the demo maps below
- Support pages for browsing/editing the demo device fleet are available at
  [http://localhost:8081/support](http://localhost:8081/support) - see
  [processing-service/README.md](processing-service/README.md#support-pages)
> **Note:** `depends_on` only gates GeoServer and the processing service on PostGIS
> being ready — it does not wait for GeoServer itself to finish initializing. If you open
> the demo maps within the first few seconds of `docker compose up`, GeoServer may still
> be loading its data directory. Give it 20–30 seconds if the maps don't appear
> immediately.

The `postgis/init` directory contains the initial schema (PostGIS extension, device
location table, cell tower cache table) and seed data, applied automatically on first
start against an empty database volume:

- `001_init.sql` - creates the PostGIS extension, `device_locations`, and
  `cell_tower_cache` tables
- `002_load_cell_towers.sql` - loads OpenCellID cell tower data for MCC 505
  (Australia) from `src-data/cell-towers/505.csv` into `cell_tower_cache`, so the
  processing service has cache hits to demonstrate out of the box
- `003_load_boundaries.sh` - loads the SAL (suburbs/localities), world country, and
  populated places shapefiles from `src-data/` into `sal_boundaries`,
  `country_boundaries`, and `populated_places`, reprojecting all to EPSG:4326 for
  GeoServer to publish as base map layers. This script depends on `shp2pgsql`, which
  is added to the PostGIS image via a custom build on top of `postgis/postgis:16-3.4`
  — it is not available in the stock image.
- `004_seed_demo_devices.sql` - seeds 1,000 demo devices (`terminal-1001` -
  `terminal-2000`) with randomized telemetry, locations weighted towards major
  Australian cities, 30 days of sales summary history, and an initial connectivity
  event each, for exercising the device/connectivity/sales API and `/support` pages.
  Merchant categories are drawn from the full reference list in
  `src-data/mcc_codes.csv`
### Geolocation provider

The processing service supports two geolocation providers, controlled by the
`GEOLOCATION_PROVIDER` environment variable:

- **`stub` (default)** - no external calls, no API key required. Returns a deterministic
  location derived from the cell tower's own identifiers. Useful for demos and
  development, but the locations are synthetic — do not mistake stub output for real
  geocoding results.
- **`google`** - calls the real Google Geolocation API. To use it, export
  `GOOGLE_GEOLOCATION_API_KEY` before running `docker compose up`.
### SAL (suburbs/localities) shapefile

`src-data/SAL_2021_AUST_GDA2020_SIMPLIFIED/` contains the ABS "Suburbs and Localities
(SAL) 2021, GDA2020" digital boundary file, simplified (`ST_SimplifyPreserveTopology`,
tolerance 0.0001 degrees, ~11m) to shrink the `.shp` from ~140MB to ~40MB so it fits
within GitHub's file size limits. This is plenty precise for the basemap rendering in
this PoC; if you need the original full-resolution boundaries, download the SAL 2021
GDA2020 shapefile from the ABS Australian Statistical Geography Standard (ASGS)
Edition 3 page on abs.gov.au and point `003_load_boundaries.sh` at it instead.

### GeoServer data directory

GeoServer's data directory is bind-mounted from `geoserver/data/` in this repo to
`/opt/geoserver/data_dir` in the container. On first run, the kartoza image populates
that folder with its default data directory. Any workspaces, stores, layers, styles
(SLDs), and layer groups you configure through the admin UI are written back to
`geoserver/data/` and can be committed, so cloning the repo and running
`docker compose up` brings up a GeoServer instance that's already configured with the
WFS layers, styles, and base maps used by this PoC.

This is a different mental model from the SQL-driven PostGIS seeding: GeoServer
configuration isn't recreated by a script on each boot — it's just files in the repo
that get bind-mounted in. If you delete `geoserver/data/` and restart, the kartoza
image will regenerate its defaults and your configured workspaces and styles will be
gone.

### Demo maps

The home page ([http://localhost:8081](http://localhost:8081)) links to two
interactive Leaflet map pages backed by the `geospatial-monitoring` GeoServer
workspace - see
[Demo map pages](processing-service/README.md#demo-map-pages) for details:

- `/demo/terminal-status` - all `device_locations`, colored by `connectivity_status`
  (online = green, expected offline = orange, unexpected offline = red), matching the
  status colors used on the `/support` pages, with a per-status layer toggle and
  legend.
- `/demo/terminal-network` - cell towers across Australia from `cell_tower_cache` (mcc
  505), colored by carrier (Telstra = blue, Optus = orange, other = grey) derived from
  `mnc` via the `cell_tower_carrier_points` and `cell_tower_carrier_coverage` SQL view
  feature types, with each tower's coverage range (`coverage`) drawn as a
  semi-translucent circle in the same carrier color, plus `device_locations` colored
  by `network_provider` (SIM provider). Layer toggles and a legend cover all of these.
  Both pages use the `demo-basemap` layer group (country boundaries, SAL
  suburbs/localities, and populated places) as the shared backdrop, served as WMS tiles
  from GeoServer. The underlying layers and styles can also be previewed directly from
  the GeoServer admin UI (Layer Preview) or via WMS `GetMap`, e.g.:

```text
http://localhost:8080/geoserver/geospatial-monitoring/wms?service=WMS&version=1.1.0&request=GetMap&layers=geospatial-monitoring:device_locations&styles=terminal-status&bbox=110,-45,155,-10&width=600&height=600&srs=EPSG:4326&format=image/png
```

The `demo-terminal-status` and `demo-terminal-network` layer groups in GeoServer
pre-date these pages and remain available for direct layer-group previews, but the
`/demo/*` pages compose the individual layers and styles directly for finer-grained
toggles and legends.

## Architecture summary

This is a local proof-of-concept for a payment-terminal geolocation pipeline, made of
three Docker Compose services:

1. **PostGIS** (`postgis/` - a custom image built on `postgis/postgis:16-3.4` with
   `shp2pgsql` added) - the spatial datastore. On first boot against an empty volume,
   four init scripts run in order: create the schema/extension, bulk-load OpenCellID
   Australian cell-tower data, shapefile-load ABS suburb/country/populated-place
   boundaries (reprojected to EPSG:4326), and seed 1,000 fake terminals with 30 days of
   synthetic telemetry/sales history.
2. **GeoServer** (`kartoza/geoserver:2.24.2`) - publishes PostGIS tables as WMS/WFS
   layers (device status, cell-tower coverage by carrier, basemap layer group). Its
   entire data directory is bind-mounted and version-controlled, so the repo ships a
   pre-configured GeoServer instance rather than relying on first-boot defaults.
3. **processing-service** - a Kotlin/Spring Boot stand-in for the real "telemetry
   processing service." It implements a cache-first location resolution chain (device
   GPS → cell-tower cache → WiFi AP cache → external geolocation API), exposes a
   small admin/demo UI (`/support`, `/demo/*`), and talks to PostGIS via plain
   `JdbcTemplate`.
   Compose wiring: GeoServer and processing-service both `depends_on: postgis` gated on its
   healthcheck (`pg_isready`), so PostGIS is guaranteed ready before either starts. Neither
   GeoServer nor the processing service has its own healthcheck or readiness probe — `docker
compose up -d` reporting "healthy" reflects PostGIS only. Only processing-service is
   parameterized by env vars (`GEOLOCATION_PROVIDER`, `GOOGLE_GEOLOCATION_API_KEY`) -
   everything else is hardcoded for local/demo convenience.

## Contents

- `docker-compose.yml` - PostGIS + GeoServer + processing-service
- `postgis/` - PostGIS image build and `init/` scripts (schema + seed data)
- `geoserver/data/` - GeoServer data directory (workspaces, stores, styles, layer groups)
- `src-data/` - source datasets (cell towers, SAL boundaries, country boundaries, and
  populated places) used to seed PostGIS
- `processing-service/` - telemetry processing service stub