from dataclasses import dataclass


@dataclass
class CellTower:
    mcc: int
    mnc: int
    lac: int
    cell_id: int


@dataclass
class Location:
    latitude: float
    longitude: float


@dataclass
class DeviceTelemetry:
    device_id: str
    cell_towers: list[CellTower]
