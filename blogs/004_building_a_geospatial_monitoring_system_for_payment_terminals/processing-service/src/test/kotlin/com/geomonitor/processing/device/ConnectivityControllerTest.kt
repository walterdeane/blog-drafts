package com.geomonitor.processing.device

import com.geomonitor.processing.model.ConnectivityEvent
import com.geomonitor.processing.model.ConnectivityStatusRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.http.HttpStatus
import java.time.Instant

class ConnectivityControllerTest {

    @Test
    fun `records a connectivity event and returns it`() {
        val deviceRepository = mock(DeviceRepository::class.java)
        val controller = ConnectivityController(deviceRepository)

        val event = ConnectivityEvent(status = "online", occurredAt = Instant.parse("2026-06-14T00:00:00Z"))
        `when`(deviceRepository.recordConnectivityEvent("terminal-1001", "online")).thenReturn(event)

        val response = controller.recordConnectivityEvent("terminal-1001", ConnectivityStatusRequest(status = "online"))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(event, response.body)
    }

    @Test
    fun `returns 404 when recording a connectivity event for an unknown device`() {
        val deviceRepository = mock(DeviceRepository::class.java)
        val controller = ConnectivityController(deviceRepository)

        `when`(deviceRepository.recordConnectivityEvent("unknown", "online")).thenReturn(null)

        val response = controller.recordConnectivityEvent("unknown", ConnectivityStatusRequest(status = "online"))

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `returns connectivity history for a device`() {
        val deviceRepository = mock(DeviceRepository::class.java)
        val controller = ConnectivityController(deviceRepository)

        val history = listOf(
            ConnectivityEvent(status = "unexpected_offline", occurredAt = Instant.parse("2026-06-14T00:00:00Z")),
            ConnectivityEvent(status = "online", occurredAt = Instant.parse("2026-06-13T00:00:00Z")),
        )
        `when`(deviceRepository.getConnectivityHistory("terminal-1001")).thenReturn(history)

        val response = controller.getConnectivityHistory("terminal-1001")

        assertEquals(history, response)
    }
}
