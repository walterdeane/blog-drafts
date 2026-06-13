import psycopg2

from .models import CellTower, Location


class LocationCache:
    """Lookup/storage of resolved device and cell tower locations in PostGIS."""

    def __init__(self, dsn: str):
        self.dsn = dsn

    def _connect(self):
        return psycopg2.connect(self.dsn)

    def get_device_location(self, device_id: str) -> Location | None:
        with self._connect() as conn, conn.cursor() as cur:
            cur.execute(
                """
                SELECT ST_Y(location::geometry), ST_X(location::geometry)
                FROM device_locations
                WHERE device_id = %s
                """,
                (device_id,),
            )
            row = cur.fetchone()
            return Location(latitude=row[0], longitude=row[1]) if row else None

    def get_cell_tower_location(self, tower: CellTower) -> Location | None:
        with self._connect() as conn, conn.cursor() as cur:
            cur.execute(
                """
                SELECT ST_Y(location::geometry), ST_X(location::geometry)
                FROM cell_tower_cache
                WHERE mcc = %s AND mnc = %s AND lac = %s AND cell_id = %s
                """,
                (tower.mcc, tower.mnc, tower.lac, tower.cell_id),
            )
            row = cur.fetchone()
            return Location(latitude=row[0], longitude=row[1]) if row else None

    def store_device_location(self, device_id: str, location: Location, source: str) -> None:
        with self._connect() as conn, conn.cursor() as cur:
            cur.execute(
                """
                INSERT INTO device_locations (device_id, location, source, last_seen)
                VALUES (%s, ST_SetSRID(ST_MakePoint(%s, %s), 4326), %s, now())
                ON CONFLICT (device_id) DO UPDATE
                    SET location = EXCLUDED.location,
                        source = EXCLUDED.source,
                        last_seen = now()
                """,
                (device_id, location.longitude, location.latitude, source),
            )
            conn.commit()

    def store_cell_tower_location(self, tower: CellTower, location: Location) -> None:
        with self._connect() as conn, conn.cursor() as cur:
            cur.execute(
                """
                INSERT INTO cell_tower_cache (mcc, mnc, lac, cell_id, location, resolved_at)
                VALUES (%s, %s, %s, %s, ST_SetSRID(ST_MakePoint(%s, %s), 4326), now())
                ON CONFLICT (mcc, mnc, lac, cell_id) DO UPDATE
                    SET location = EXCLUDED.location,
                        resolved_at = now()
                """,
                (tower.mcc, tower.mnc, tower.lac, tower.cell_id, location.longitude, location.latitude),
            )
            conn.commit()
