# Processing Service

A minimal stand-in for the "Telemetry Processing Service" described in the blog post.

It's a Kotlin/Spring Boot application that accepts device telemetry over HTTP and resolves
each device's location using the caching strategy described in the post:

1. Check whether the device already has a known location.
2. Check the cell tower cache for the device's current tower(s).
3. Check the WiFi access point cache, prioritizing the AP the device is currently
   connected/associated with, then falling back to the other scanned BSSIDs.
4. Fall back to a geolocation provider only if none of the above are cached, then cache
   the result against every reported cell tower and access point.

Cell towers are matched on `(mcc, mnc, lac, cell_id)` and WiFi access points on `bssid`
(the AP's MAC address, case-insensitive) - both are exact-match lookups against their
own cache table (`cell_tower_cache` / `wifi_location_cache`), mirroring how each signal
type uniquely identifies a fixed physical transmitter.

A device typically reports one cell tower but a list of ~10 WiFi access points, whose
membership and order change scan-to-scan as APs go on/offline or get replaced. Caching
per-BSSID (rather than per-scan-list) tolerates this naturally - matching is per access
point, not per set, so any single previously-seen BSSID is enough for a cache hit.
Within that list, the `connected` flag marks the AP the device is actively associated
with (Android: `WifiInfo.getBSSID()`) - the WiFi analog of "the cell tower I'm camped
on" - and is checked first before falling back to the other scanned APs.

This is intentionally minimal - it does not yet cover fraud signals, incident detection,
or business intelligence feeds described in the full architecture. Those are left as future
extensions.

## Stack

- Kotlin + Spring Boot Web
- `JdbcTemplate` against PostGIS for the location cache
- Spring's `RestClient` for calling the Google Geolocation API (when enabled)

The service listens on port `8081` by default (configurable via `SERVER_PORT`), to avoid
clashing with GeoServer on `8080`.

## Geolocation provider

The cell-tower fallback is pluggable via `app.geolocation.provider` (env var
`GEOLOCATION_PROVIDER`):

- `stub` (default) - returns a deterministic fake location derived from the cell tower's
  identifiers. No external dependencies or API key required - good for running the demo
  end-to-end.
- `google` - calls the real [Google Geolocation API](https://developers.google.com/maps/documentation/geolocation/overview).
  Requires `GOOGLE_GEOLOCATION_API_KEY`.

The resolved `source` field reflects which provider was used (`cache`, `stub`, or `google`).

## Testing the service end-to-end (Docker Compose)

This is the easiest way to try the full request flow against a real PostGIS database,
using the default `stub` geolocation provider.

1. From the **repo root** (`blogs/004_building_a_geospatial_monitoring_system_for_payment_terminals/`),
   build and start PostGIS and the processing service:

   ```bash
   docker compose up -d --build postgis processing-service
   ```

   Wait for both containers to report healthy/started:

   ```bash
   docker compose ps
   ```

2. Send a sample telemetry request. The first request for a given cell tower has no
   cached location, so it falls back to the `stub` provider:

   ```bash
   curl -s -X POST http://localhost:8081/telemetry \
     -H 'Content-Type: application/json' \
     -d '{
       "device_id": "terminal-123",
       "cell_towers": [
         {"mcc": 234, "mnc": 15, "lac": 1234, "cell_id": 5678}
       ]
     }'
   ```

   Expected response (`source: "stub"`). `accuracyMeters` is the resolved cell's
   estimated range, also stored as `cell_tower_cache.range_m`/`coverage`:

   ```json
   {"device_id":"terminal-123","location":{"latitude":-5.12,"longitude":-118.57,"accuracyMeters":3932.0},"source":"stub"}
   ```

3. Send the same request again. The device now has a cached location, so the response
   comes straight from PostGIS:

   ```json
   {"device_id":"terminal-123","location":{"latitude":-5.12,"longitude":-118.57},"source":"cache"}
   ```

4. (Optional) Send a request for a **different** device on the **same** cell tower -
   it hits the cell-tower cache instead:

   ```bash
   curl -s -X POST http://localhost:8081/telemetry \
     -H 'Content-Type: application/json' \
     -d '{
       "device_id": "terminal-456",
       "cell_towers": [
         {"mcc": 234, "mnc": 15, "lac": 1234, "cell_id": 5678}
       ]
     }'
   ```

   Expected: same coordinates, `"source":"cache"`.

5. (Optional) Inspect the cache tables directly in PostGIS:

   ```bash
   docker compose exec postgis psql -U geospatial -d geospatial \
     -c "SELECT device_id, source, ST_AsText(location::geometry) FROM device_locations;"
   ```

6. Tear down when done:

   ```bash
   docker compose down
   ```

To exercise the real Google Geolocation API instead of the stub, export
`GEOLOCATION_PROVIDER=google` and `GOOGLE_GEOLOCATION_API_KEY=...` before step 1
(`docker compose up -d --build` picks these up via the `processing-service` environment
block in `docker-compose.yml`).

## Running locally without Docker

Requires a PostGIS instance reachable from your machine (e.g. `docker compose up -d postgis`
from the repo root).

[`run-service.sh`](run-service.sh) sets every configurable environment variable in one
place and starts the app via `bootRun`:

```bash
./run-service.sh
```

To change settings, edit the `export` lines at the top of the script (or override them
in your shell before running it) - e.g. to use the real Google Geolocation API instead
of the stub, set:

```bash
export GEOLOCATION_PROVIDER=google
export GOOGLE_GEOLOCATION_API_KEY=...
./run-service.sh
```

Then use the same `curl` commands as above against `http://localhost:8081`.

## API

### `POST /telemetry`

Resolves a device's location and updates the cache.

```bash
curl -X POST http://localhost:8081/telemetry \
  -H 'Content-Type: application/json' \
  -d '{
    "device_id": "terminal-123",
    "cell_towers": [
      {"mcc": 234, "mnc": 15, "lac": 1234, "cell_id": 5678}
    ]
  }'
```

```json
{
  "device_id": "terminal-123",
  "location": {"latitude": 51.5074, "longitude": -0.1278, "accuracyMeters": 3932.0},
  "source": "stub"
}
```

`wifi_access_points` is accepted alongside or instead of `cell_towers`, each identified
by its `mac_address` (BSSID). `connected` is optional (defaults to `false`) and marks
the AP the device is actively associated with, as opposed to the other APs merely seen
in its scan results:

```bash
curl -X POST http://localhost:8081/telemetry \
  -H 'Content-Type: application/json' \
  -d '{
    "device_id": "terminal-123",
    "wifi_access_points": [
      {"mac_address": "AA:BB:CC:DD:EE:FF", "signal_strength": -55, "connected": true},
      {"mac_address": "11:22:33:44:55:66", "signal_strength": -70}
    ]
  }'
```

## Building and unit tests

```bash
./gradlew build   # compiles and runs unit tests
./gradlew test    # unit tests only
```

## Docker

```bash
docker build -t processing-service .
docker run --rm -p 8081:8081 \
  -e POSTGIS_HOST=postgis \
  -e GOOGLE_GEOLOCATION_API_KEY=... \
  processing-service
```

Or run the whole stack (PostGIS, GeoServer, processing-service) from the repo root:

```bash
docker compose up -d
```
