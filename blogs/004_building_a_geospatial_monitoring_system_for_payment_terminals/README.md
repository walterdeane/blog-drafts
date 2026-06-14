# Building a Geospatial Monitoring System for Payment Terminals

Proof-of-concept resources accompanying the blog post on building a geospatial monitoring
system for payment terminals (and other connected devices).

The full proposed architecture covers AWS IoT ingestion, a telemetry processing service,
a location cache, Aurora PostgreSQL + PostGIS, and GeoServer/OpenLayers for mapping. This
PoC focuses on the parts that can run locally:

- **PostGIS** - spatial storage for device locations and the cell tower cache
- **GeoServer** - map/feature services on top of PostGIS
- **processing-service** - a minimal Kotlin/Spring Boot stand-in for the telemetry
  processing service, demonstrating the cache-first location resolution strategy from
  the post

## Quick Start

### Start everything

```bash
docker compose up -d
```

This brings up PostGIS, GeoServer, and the processing service together:

- PostGIS is available on `localhost:5432` (db `geospatial`, user/password `geospatial`)
- GeoServer admin UI is available at [http://localhost:8080/geoserver](http://localhost:8080/geoserver)
  (user `admin`, password `geoserver`)
- The processing service is available on `localhost:8081` - see
  [processing-service/README.md](processing-service/README.md) for its API

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
  GeoServer to publish as base map layers

To use the Google Geolocation API fallback, export `GOOGLE_GEOLOCATION_API_KEY` before
running `docker compose up`.

### GeoServer data directory

GeoServer's data directory is bind-mounted from `geoserver/data/` in this repo to
`/opt/geoserver/data_dir` in the container. On first run, the kartoza image populates
that folder with its default data directory. Any workspaces, stores, layers, styles
(SLDs), and layer groups you configure through the admin UI are written back to
`geoserver/data/` and can be committed, so cloning the repo and running
`docker compose up` brings up a GeoServer instance that's already configured with the
WFS layers, styles, and base maps used by this PoC.

## Contents

- `docker-compose.yml` - PostGIS + GeoServer + processing-service
- `postgis/` - PostGIS image build and `init/` scripts (schema + seed data)
- `geoserver/data/` - GeoServer data directory (workspaces, stores, styles, layer groups)
- `src-data/` - source datasets (cell towers, SAL boundaries, country boundaries, and
  populated places) used to seed PostGIS
- `processing-service/` - telemetry processing service stub
