package com.geomonitor.processing.device

import com.geomonitor.processing.model.ConnectivityEvent
import com.geomonitor.processing.model.DeviceDetail
import com.geomonitor.processing.model.DeviceSummary
import com.geomonitor.processing.model.DeviceTelemetryAttributes
import com.geomonitor.processing.model.Location
import com.geomonitor.processing.model.SalesHeatmapPoint
import com.geomonitor.processing.model.SalesSummaryEntry
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate

/**
 * Lookup/update of device telemetry, connectivity history, and sales summaries -
 * backing the `/api/devices*` endpoints and the `/support` pages.
 */
@Repository
class DeviceRepository(private val jdbcTemplate: JdbcTemplate) {

    fun listDevices(): List<DeviceSummary> =
        jdbcTemplate.query(
            """
            SELECT device_id, ST_Y(location::geometry) AS lat, ST_X(location::geometry) AS lon,
                   merchant_category_name, connectivity_status, battery_pct,
                   network_interface, network_provider, last_seen
            FROM device_locations
            ORDER BY device_id
            """.trimIndent(),
        ) { rs, _ ->
            DeviceSummary(
                deviceId = rs.getString("device_id"),
                location = Location(latitude = rs.getDouble("lat"), longitude = rs.getDouble("lon")),
                merchantCategoryName = rs.getString("merchant_category_name"),
                connectivityStatus = rs.getString("connectivity_status"),
                batteryPct = rs.getObject("battery_pct", Integer::class.java) as Int?,
                networkInterface = rs.getString("network_interface"),
                networkProvider = rs.getString("network_provider"),
                lastSeen = rs.getTimestamp("last_seen").toInstant(),
            )
        }

    fun getDevice(deviceId: String): DeviceDetail? =
        jdbcTemplate.query(
            """
            SELECT device_id, ST_Y(location::geometry) AS lat, ST_X(location::geometry) AS lon,
                   source, last_seen, connectivity_status, connectivity_status_at,
                   merchant_category_code, merchant_category_name, battery_pct, memory_used_pct,
                   network_interface, network_provider, sim1_id, sim2_id, active_sim_slot
            FROM device_locations
            WHERE device_id = ?
            """.trimIndent(),
            { rs, _ -> toDeviceDetail(rs) },
            deviceId,
        ).firstOrNull()

    fun updateTelemetry(deviceId: String, attrs: DeviceTelemetryAttributes): Boolean =
        jdbcTemplate.update(
            """
            UPDATE device_locations SET
                merchant_category_code = ?,
                merchant_category_name = ?,
                battery_pct = ?,
                memory_used_pct = ?,
                network_interface = ?,
                network_provider = ?,
                sim1_id = ?,
                sim2_id = ?,
                active_sim_slot = ?
            WHERE device_id = ?
            """.trimIndent(),
            attrs.merchantCategoryCode, attrs.merchantCategoryName,
            attrs.batteryPct, attrs.memoryUsedPct,
            attrs.networkInterface, attrs.networkProvider,
            attrs.sim1Id, attrs.sim2Id, attrs.activeSimSlot,
            deviceId,
        ) > 0

    /**
     * Records an LWT-style connectivity event and updates the device's current
     * connectivity status. Returns null if no such device exists.
     */
    fun recordConnectivityEvent(deviceId: String, status: String): ConnectivityEvent? {
        val occurredAt = Instant.now()
        val updated = jdbcTemplate.update(
            "UPDATE device_locations SET connectivity_status = ?, connectivity_status_at = ? WHERE device_id = ?",
            status, Timestamp.from(occurredAt), deviceId,
        )
        if (updated == 0) return null

        jdbcTemplate.update(
            "INSERT INTO device_connectivity_events (device_id, status, occurred_at) VALUES (?, ?, ?)",
            deviceId, status, Timestamp.from(occurredAt),
        )
        return ConnectivityEvent(status = status, occurredAt = occurredAt)
    }

    fun getConnectivityHistory(deviceId: String): List<ConnectivityEvent> =
        jdbcTemplate.query(
            """
            SELECT status, occurred_at
            FROM device_connectivity_events
            WHERE device_id = ?
            ORDER BY occurred_at DESC
            LIMIT 50
            """.trimIndent(),
            { rs, _ -> ConnectivityEvent(status = rs.getString("status"), occurredAt = rs.getTimestamp("occurred_at").toInstant()) },
            deviceId,
        )

    /**
     * Most recent date with sales data, used to anchor the sales-by-day demo
     * map's date selector to a date the seeded data actually covers.
     */
    fun getLatestSalesDate(): LocalDate? =
        jdbcTemplate.queryForObject(
            "SELECT MAX(summary_date) FROM device_sales_summary",
            java.sql.Date::class.java,
        )?.toLocalDate()

    fun getSalesSummary(deviceId: String): List<SalesSummaryEntry> =
        jdbcTemplate.query(
            """
            SELECT summary_date, transaction_count, sales_total_cents
            FROM device_sales_summary
            WHERE device_id = ?
            ORDER BY summary_date
            """.trimIndent(),
            { rs, _ ->
                SalesSummaryEntry(
                    summaryDate = rs.getDate("summary_date").toLocalDate(),
                    transactionCount = rs.getInt("transaction_count"),
                    salesTotalCents = rs.getLong("sales_total_cents"),
                )
            },
            deviceId,
        )

    /**
     * Total sales per device over its summary history, optionally restricted to a
     * bounding box - a sample geospatial query for heatmap-style visualisation.
     */
    fun getSalesHeatmap(minLat: Double?, minLon: Double?, maxLat: Double?, maxLon: Double?): List<SalesHeatmapPoint> {
        val bbox = listOf(minLat, minLon, maxLat, maxLon)
        val whereClause = if (bbox.all { it != null }) {
            "WHERE ST_MakeEnvelope(?, ?, ?, ?, 4326)::geography && dl.location"
        } else {
            ""
        }
        val sql = """
            SELECT dl.device_id, ST_Y(dl.location::geometry) AS lat, ST_X(dl.location::geometry) AS lon,
                   SUM(s.sales_total_cents) AS sales_total_cents
            FROM device_locations dl
            JOIN device_sales_summary s ON s.device_id = dl.device_id
            $whereClause
            GROUP BY dl.device_id, dl.location
            ORDER BY sales_total_cents DESC
        """.trimIndent()

        val params = if (bbox.all { it != null }) arrayOf(minLon, minLat, maxLon, maxLat) else emptyArray()

        return jdbcTemplate.query(sql, { rs, _ ->
            SalesHeatmapPoint(
                deviceId = rs.getString("device_id"),
                location = Location(latitude = rs.getDouble("lat"), longitude = rs.getDouble("lon")),
                salesTotalCents = rs.getLong("sales_total_cents"),
            )
        }, *params)
    }

    private fun toDeviceDetail(rs: ResultSet): DeviceDetail =
        DeviceDetail(
            deviceId = rs.getString("device_id"),
            location = Location(latitude = rs.getDouble("lat"), longitude = rs.getDouble("lon")),
            source = rs.getString("source"),
            lastSeen = rs.getTimestamp("last_seen").toInstant(),
            connectivityStatus = rs.getString("connectivity_status"),
            connectivityStatusAt = rs.getTimestamp("connectivity_status_at").toInstant(),
            telemetry = DeviceTelemetryAttributes(
                merchantCategoryCode = rs.getObject("merchant_category_code", Integer::class.java) as Int?,
                merchantCategoryName = rs.getString("merchant_category_name"),
                batteryPct = rs.getObject("battery_pct", Integer::class.java) as Int?,
                memoryUsedPct = rs.getObject("memory_used_pct", Integer::class.java) as Int?,
                networkInterface = rs.getString("network_interface"),
                networkProvider = rs.getString("network_provider"),
                sim1Id = rs.getString("sim1_id"),
                sim2Id = rs.getString("sim2_id"),
                activeSimSlot = rs.getObject("active_sim_slot", Integer::class.java) as Int?,
            ),
        )
}
