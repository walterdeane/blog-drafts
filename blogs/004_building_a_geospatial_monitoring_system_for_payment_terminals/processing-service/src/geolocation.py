import requests

from .models import CellTower, Location

GOOGLE_GEOLOCATION_URL = "https://www.googleapis.com/geolocation/v1/geolocate"


class GoogleGeolocationClient:
    """Thin wrapper around the Google Geolocation API.

    This should only be called when no cached location is available -
    see the caching strategy described in the blog post.
    """

    def __init__(self, api_key: str):
        self.api_key = api_key

    def resolve(self, cell_towers: list[CellTower]) -> Location:
        payload = {
            "considerIp": False,
            "cellTowers": [
                {
                    "mobileCountryCode": tower.mcc,
                    "mobileNetworkCode": tower.mnc,
                    "locationAreaCode": tower.lac,
                    "cellId": tower.cell_id,
                }
                for tower in cell_towers
            ],
        }

        response = requests.post(
            GOOGLE_GEOLOCATION_URL,
            params={"key": self.api_key},
            json=payload,
            timeout=5,
        )
        response.raise_for_status()
        data = response.json()["location"]
        return Location(latitude=data["lat"], longitude=data["lng"])
