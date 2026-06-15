package com.geomonitor.processing.device

import com.geomonitor.processing.model.DeviceDetail
import com.geomonitor.processing.model.DeviceSummary
import com.geomonitor.processing.model.DeviceTelemetryAttributes
import com.geomonitor.processing.model.Location
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.http.HttpStatus
import java.time.Instant

class DeviceControllerTest {

    @Test
    fun `lists devices from the repository`() {
        val deviceRepository = mock(DeviceRepository::class.java)
        val controller = DeviceController(deviceRepository)

        val summary = DeviceSummary(
            deviceId = "terminal-1001",
            location = Location(latitude = -33.8688, longitude = 151.2093),
            merchantCategoryName = "Coffee Shop",
            connectivityStatus = "online",
            batteryPct = 80,
            networkInterface = "WIFI",
            networkProvider = "Optus",
            lastSeen = Instant.parse("2026-06-14T00:00:00Z"),
        )
        `when`(deviceRepository.listDevices()).thenReturn(listOf(summary))

        val response = controller.listDevices()

        assertEquals(listOf(summary), response)
    }

    @Test
    fun `returns device detail when found`() {
        val deviceRepository = mock(DeviceRepository::class.java)
        val controller = DeviceController(deviceRepository)

        val detail = DeviceDetail(
            deviceId = "terminal-1001",
            location = Location(latitude = -33.8688, longitude = 151.2093),
            source = "seed",
            lastSeen = Instant.parse("2026-06-14T00:00:00Z"),
            connectivityStatus = "online",
            connectivityStatusAt = Instant.parse("2026-06-14T00:00:00Z"),
            telemetry = DeviceTelemetryAttributes(batteryPct = 80),
        )
        `when`(deviceRepository.getDevice("terminal-1001")).thenReturn(detail)

        val response = controller.getDevice("terminal-1001")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(detail, response.body)
    }

    @Test
    fun `returns 404 when device not found`() {
        val deviceRepository = mock(DeviceRepository::class.java)
        val controller = DeviceController(deviceRepository)

        `when`(deviceRepository.getDevice("unknown")).thenReturn(null)

        val response = controller.getDevice("unknown")

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `updates telemetry and returns the refreshed device`() {
        val deviceRepository = mock(DeviceRepository::class.java)
        val controller = DeviceController(deviceRepository)

        val attributes = DeviceTelemetryAttributes(batteryPct = 42, networkInterface = "MOBILE")
        val updated = DeviceDetail(
            deviceId = "terminal-1001",
            location = Location(latitude = -33.8688, longitude = 151.2093),
            source = "seed",
            lastSeen = Instant.parse("2026-06-14T00:00:00Z"),
            connectivityStatus = "online",
            connectivityStatusAt = Instant.parse("2026-06-14T00:00:00Z"),
            telemetry = attributes,
        )
        `when`(deviceRepository.updateTelemetry("terminal-1001", attributes)).thenReturn(true)
        `when`(deviceRepository.getDevice("terminal-1001")).thenReturn(updated)

        val response = controller.updateTelemetry("terminal-1001", attributes)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(updated, response.body)
    }

    @Test
    fun `returns 404 when updating telemetry for an unknown device`() {
        val deviceRepository = mock(DeviceRepository::class.java)
        val controller = DeviceController(deviceRepository)

        val attributes = DeviceTelemetryAttributes(batteryPct = 42)
        `when`(deviceRepository.updateTelemetry("unknown", attributes)).thenReturn(false)

        val response = controller.updateTelemetry("unknown", attributes)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNull(response.body)
    }
}
