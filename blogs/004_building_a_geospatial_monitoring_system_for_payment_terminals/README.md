# Building a Geospatial Monitoring System for Payment Terminals

Proof-of-concept resources accompanying the blog post on building a geospatial monitoring
system for payment terminals (and other connected devices).

The full proposed architecture covers AWS IoT ingestion, a telemetry processing service,
a location cache, Aurora PostgreSQL + PostGIS, and GeoServer/OpenLayers for mapping. This
PoC focuses on the parts that can run locally:

- **PostGIS** - spatial storage for device locations and the cell tower cache
- **GeoServer** - map/feature services on top of PostGIS
- **processing-service** - a minimal stand-in for the telemetry processing service,
  demonstrating the cache-first location resolution strategy from the post

## Quick Start

### 1. Start PostGIS and GeoServer

```bash
docker compose up -d
```

- PostGIS is available on `localhost:5432` (db `geospatial`, user/password `geospatial`)
- GeoServer admin UI is available at [http://localhost:8080/geoserver](http://localhost:8080/geoserver)
  (user `admin`, password `geoserver`)

The `postgis/init` directory contains the initial schema (PostGIS extension, device
location table, cell tower cache table).

### 2. Run the processing service

See [processing-service/README.md](processing-service/README.md).

## Contents

- `docker-compose.yml` - PostGIS + GeoServer
- `postgis/init/` - schema setup, applied automatically on first start
- `processing-service/` - telemetry processing service stub
