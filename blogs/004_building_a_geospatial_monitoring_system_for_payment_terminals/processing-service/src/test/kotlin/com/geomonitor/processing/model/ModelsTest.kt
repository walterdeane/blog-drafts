package com.geomonitor.processing.model

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ModelsTest {

    private val objectMapper = JsonMapper.builder().addModule(kotlinModule()).build()

    @Test
    fun `deserializes telemetry from snake_case JSON`() {
        val json = """
            {
              "device_id": "terminal-123",
              "cell_towers": [
                {"mcc": 234, "mnc": 15, "lac": 1234, "cell_id": 5678}
              ]
            }
        """.trimIndent()

        val telemetry = objectMapper.readValue(json, DeviceTelemetry::class.java)

        assertEquals("terminal-123", telemetry.deviceId)
        assertEquals(1, telemetry.cellTowers.size)
        assertEquals(CellTower(mcc = 234, mnc = 15, lac = 1234, cellId = 5678), telemetry.cellTowers[0])
        assertEquals(emptyList<WifiAccessPoint>(), telemetry.wifiAccessPoints)
    }

    @Test
    fun `deserializes telemetry with wifi access points`() {
        val json = """
            {
              "device_id": "terminal-123",
              "wifi_access_points": [
                {"mac_address": "AA:BB:CC:DD:EE:FF", "signal_strength": -55}
              ]
            }
        """.trimIndent()

        val telemetry = objectMapper.readValue(json, DeviceTelemetry::class.java)

        assertEquals("terminal-123", telemetry.deviceId)
        assertEquals(emptyList<CellTower>(), telemetry.cellTowers)
        assertEquals(1, telemetry.wifiAccessPoints.size)
        assertEquals(
            WifiAccessPoint(macAddress = "AA:BB:CC:DD:EE:FF", signalStrengthDbm = -55),
            telemetry.wifiAccessPoints[0],
        )
        assertEquals(false, telemetry.wifiAccessPoints[0].connected)
    }

    @Test
    fun `deserializes connected flag on wifi access points`() {
        val json = """
            {
              "device_id": "terminal-123",
              "wifi_access_points": [
                {"mac_address": "AA:BB:CC:DD:EE:FF", "signal_strength": -55, "connected": true}
              ]
            }
        """.trimIndent()

        val telemetry = objectMapper.readValue(json, DeviceTelemetry::class.java)

        assertEquals(
            WifiAccessPoint(macAddress = "AA:BB:CC:DD:EE:FF", signalStrengthDbm = -55, connected = true),
            telemetry.wifiAccessPoints[0],
        )
    }

    /**
     * Mirrors the combined cell tower + WiFi access point request shape sent to the
     * Google Geolocation API:
     *
     * ```json
     * {
     *   "considerIp": false,
     *   "cellTowers": [
     *     {"cellId": 1234567, "locationAreaCode": 5678, "mobileCountryCode": 310, "mobileNetworkCode": 410}
     *   ],
     *   "wifiAccessPoints": [
     *     {"macAddress": "00:25:9c:cf:1c:ac"}
     *   ]
     * }
     * ```
     *
     * The device telemetry carries the same identifiers under its own snake_case
     * field names (`mcc`/`mnc`/`lac`/`cell_id`, `mac_address`).
     */
    @Test
    fun `deserializes combined cell tower and wifi telemetry matching the Google Geolocation API shape`() {
        val json = """
            {
              "device_id": "terminal-123",
              "cell_towers": [
                {"mcc": 310, "mnc": 410, "lac": 5678, "cell_id": 1234567}
              ],
              "wifi_access_points": [
                {"mac_address": "00:25:9c:cf:1c:ac"}
              ]
            }
        """.trimIndent()

        val telemetry = objectMapper.readValue(json, DeviceTelemetry::class.java)

        assertEquals(
            CellTower(mcc = 310, mnc = 410, lac = 5678, cellId = 1234567),
            telemetry.cellTowers[0],
        )
        assertEquals(
            WifiAccessPoint(macAddress = "00:25:9c:cf:1c:ac"),
            telemetry.wifiAccessPoints[0],
        )
        assertEquals(null, telemetry.gpsLocation)
    }

    @Test
    fun `deserializes telemetry with a device-reported gps location`() {
        val json = """
            {
              "device_id": "terminal-123",
              "cell_towers": [
                {"mcc": 310, "mnc": 410, "lac": 5678, "cell_id": 1234567}
              ],
              "wifi_access_points": [
                {"mac_address": "00:25:9c:cf:1c:ac"}
              ],
              "gps_location": {"latitude": -33.8688, "longitude": 151.2093, "accuracyMeters": 12.5}
            }
        """.trimIndent()

        val telemetry = objectMapper.readValue(json, DeviceTelemetry::class.java)

        assertEquals(
            Location(latitude = -33.8688, longitude = 151.2093, accuracyMeters = 12.5),
            telemetry.gpsLocation,
        )
    }

    @Test
    fun `gps_location is absent by default`() {
        val json = """
            {
              "device_id": "terminal-123",
              "cell_towers": [
                {"mcc": 310, "mnc": 410, "lac": 5678, "cell_id": 1234567}
              ]
            }
        """.trimIndent()

        val telemetry = objectMapper.readValue(json, DeviceTelemetry::class.java)

        assertEquals(null, telemetry.gpsLocation)
    }
}
