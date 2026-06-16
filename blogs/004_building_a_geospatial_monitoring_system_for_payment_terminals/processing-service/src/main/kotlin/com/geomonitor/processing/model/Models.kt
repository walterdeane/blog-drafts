package com.geomonitor.processing.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class CellTower(
    val mcc: Int,
    val mnc: Int,
    val lac: Int,
    @JsonProperty("cell_id") val cellId: Int,
    @JsonProperty("signal_strength") val signalStrengthDbm: Int? = null,
)

data class WifiAccessPoint(
    @JsonProperty("mac_address") val macAddress: String,
    @JsonProperty("signal_strength") val signalStrengthDbm: Int? = null,
    @JsonProperty("connected") val connected: Boolean = false,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Location(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Double? = null,
)

data class DeviceTelemetry(
    @JsonProperty("device_id") val deviceId: String,
    @JsonProperty("cell_towers") val cellTowers: List<CellTower> = emptyList(),
    @JsonProperty("wifi_access_points") val wifiAccessPoints: List<WifiAccessPoint> = emptyList(),
    @JsonProperty("gps_location") val gpsLocation: Location? = null,
    @JsonProperty("radio_type") val radioType: String? = null,
    @JsonProperty("bypass_cache") val bypassCache: Boolean = false,
)
