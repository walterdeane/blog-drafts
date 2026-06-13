import json
import os

import boto3

from .cache import LocationCache
from .geolocation import GoogleGeolocationClient
from .models import CellTower, DeviceTelemetry, Location

SQS_QUEUE_URL = os.environ["SQS_QUEUE_URL"]
POSTGIS_DSN = os.environ.get(
    "POSTGIS_DSN", "dbname=geospatial user=geospatial password=geospatial host=localhost"
)
GOOGLE_API_KEY = os.environ.get("GOOGLE_GEOLOCATION_API_KEY", "")


def resolve_location(
    telemetry: DeviceTelemetry, cache: LocationCache, geolocation: GoogleGeolocationClient
) -> tuple[Location, str]:
    """Resolve a device's location, following the caching strategy from the blog post:

    1. Check whether the device already has a known location.
    2. Check the cell tower cache for the device's current tower(s).
    3. Fall back to the Google Geolocation API if neither is cached.

    Returns the resolved location and the source it came from ('cache' or 'google').
    """
    cached_device_location = cache.get_device_location(telemetry.device_id)
    if cached_device_location:
        return cached_device_location, "cache"

    for tower in telemetry.cell_towers:
        cached_tower_location = cache.get_cell_tower_location(tower)
        if cached_tower_location:
            return cached_tower_location, "cache"

    location = geolocation.resolve(telemetry.cell_towers)
    for tower in telemetry.cell_towers:
        cache.store_cell_tower_location(tower, location)
    return location, "google"


def parse_telemetry(message_body: str) -> DeviceTelemetry:
    data = json.loads(message_body)
    return DeviceTelemetry(
        device_id=data["device_id"],
        cell_towers=[CellTower(**tower) for tower in data["cell_towers"]],
    )


def main() -> None:
    sqs = boto3.client("sqs")
    cache = LocationCache(POSTGIS_DSN)
    geolocation = GoogleGeolocationClient(GOOGLE_API_KEY)

    while True:
        response = sqs.receive_message(QueueUrl=SQS_QUEUE_URL, WaitTimeSeconds=20, MaxNumberOfMessages=10)
        for message in response.get("Messages", []):
            telemetry = parse_telemetry(message["Body"])
            location, source = resolve_location(telemetry, cache, geolocation)
            cache.store_device_location(telemetry.device_id, location, source=source)
            sqs.delete_message(QueueUrl=SQS_QUEUE_URL, ReceiptHandle=message["ReceiptHandle"])


if __name__ == "__main__":
    main()
