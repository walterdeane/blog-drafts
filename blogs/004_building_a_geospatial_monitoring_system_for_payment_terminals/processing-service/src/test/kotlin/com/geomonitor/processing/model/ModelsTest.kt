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
}
