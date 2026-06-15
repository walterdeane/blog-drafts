package com.geomonitor.processing.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.time.LocalDate

/**
 * Editable device telemetry/business attributes, distinct from the location-resolution
 * telemetry in [DeviceTelemetry]. "merchant_category_*" identifies the kind of business
 * the device is deployed at (e.g. coffee shop, clothing store) - not to be confused
 * with [CellTower.mcc] (mobile country code).
 */
data class DeviceTelemetryAttributes(
    @JsonProperty("merchant_category_code") val merchantCategoryCode: Int? = null,
    @JsonProperty("merchant_category_name") val merchantCategoryName: String? = null,
    @JsonProperty("battery_pct") val batteryPct: Int? = null,
    @JsonProperty("memory_used_pct") val memoryUsedPct: Int? = null,
    @JsonProperty("network_interface") val networkInterface: String? = null,
    @JsonProperty("network_provider") val networkProvider: String? = null,
    @JsonProperty("sim1_id") val sim1Id: String? = null,
    @JsonProperty("sim2_id") val sim2Id: String? = null,
    @JsonProperty("active_sim_slot") val activeSimSlot: Int? = null,
)

/** Row in the device list / support pages. */
data class DeviceSummary(
    @JsonProperty("device_id") val deviceId: String,
    val location: Location,
    @JsonProperty("merchant_category_name") val merchantCategoryName: String?,
    @JsonProperty("connectivity_status") val connectivityStatus: String,
    @JsonProperty("battery_pct") val batteryPct: Int?,
    @JsonProperty("network_interface") val networkInterface: String?,
    @JsonProperty("network_provider") val networkProvider: String?,
    @JsonProperty("last_seen") val lastSeen: Instant,
)

/** Full device record, including its editable telemetry attributes. */
data class DeviceDetail(
    @JsonProperty("device_id") val deviceId: String,
    val location: Location,
    val source: String,
    @JsonProperty("last_seen") val lastSeen: Instant,
    @JsonProperty("connectivity_status") val connectivityStatus: String,
    @JsonProperty("connectivity_status_at") val connectivityStatusAt: Instant,
    val telemetry: DeviceTelemetryAttributes,
)

/** Body of a simulated LWT-style connectivity message. */
data class ConnectivityStatusRequest(val status: String)

/** A single entry in a device's connectivity audit log. */
data class ConnectivityEvent(
    val status: String,
    @JsonProperty("occurred_at") val occurredAt: Instant,
)

/** One day's sales summary for a device. */
data class SalesSummaryEntry(
    @JsonProperty("summary_date") val summaryDate: LocalDate,
    @JsonProperty("transaction_count") val transactionCount: Int,
    @JsonProperty("sales_total_cents") val salesTotalCents: Long,
)

/** A device's total sales over its summary history, for heatmap-style queries. */
data class SalesHeatmapPoint(
    @JsonProperty("device_id") val deviceId: String,
    val location: Location,
    @JsonProperty("sales_total_cents") val salesTotalCents: Long,
)
