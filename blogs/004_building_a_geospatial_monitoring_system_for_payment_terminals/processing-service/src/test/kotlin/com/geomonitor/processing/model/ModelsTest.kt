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
    }
}
