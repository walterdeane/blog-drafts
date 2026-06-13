# Processing Service

A minimal stand-in for the "Telemetry Processing Service" described in the blog post.

It consumes device telemetry from an SQS queue and resolves each device's location using
the caching strategy described in the post:

1. Check whether the device already has a known location.
2. Check the cell tower cache for the device's current tower(s).
3. Fall back to the Google Geolocation API only if neither is cached, then cache the result.

This is intentionally minimal - it does not yet cover fraud signals, incident detection,
or business intelligence feeds described in the full architecture. Those are left as future
extensions.

## Running locally

```bash
pip install -r requirements.txt

export SQS_QUEUE_URL=...
export POSTGIS_DSN="dbname=geospatial user=geospatial password=geospatial host=localhost"
export GOOGLE_GEOLOCATION_API_KEY=...

python -m src.main
```
