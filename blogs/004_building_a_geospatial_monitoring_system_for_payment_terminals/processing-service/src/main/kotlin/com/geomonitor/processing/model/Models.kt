package com.geomonitor.processing.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class CellTower(
    val mcc: Int,
    val mnc: Int,
    val lac: Int,
    @JsonProperty("cell_id") val cellId: Int,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Location(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Double? = null,
)

data class DeviceTelemetry(
    @JsonProperty("device_id") val deviceId: String,
    @JsonProperty("cell_towers") val cellTowers: List<CellTower>,
)
