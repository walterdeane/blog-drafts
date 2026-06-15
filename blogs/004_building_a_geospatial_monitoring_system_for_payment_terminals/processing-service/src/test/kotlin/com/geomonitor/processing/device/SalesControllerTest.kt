package com.geomonitor.processing.device

import com.geomonitor.processing.model.Location
import com.geomonitor.processing.model.SalesHeatmapPoint
import com.geomonitor.processing.model.SalesSummaryEntry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDate

class SalesControllerTest {

    @Test
    fun `returns sales summary for a device`() {
        val deviceRepository = mock(DeviceRepository::class.java)
        val controller = SalesController(deviceRepository)

        val summary = listOf(
            SalesSummaryEntry(summaryDate = LocalDate.parse("2026-06-13"), transactionCount = 12, salesTotalCents = 34500),
            SalesSummaryEntry(summaryDate = LocalDate.parse("2026-06-14"), transactionCount = 9, salesTotalCents = 27800),
        )
        `when`(deviceRepository.getSalesSummary("terminal-1001")).thenReturn(summary)

        val response = controller.getSalesSummary("terminal-1001")

        assertEquals(summary, response)
    }

    @Test
    fun `returns sales heatmap without a bounding box`() {
        val deviceRepository = mock(DeviceRepository::class.java)
        val controller = SalesController(deviceRepository)

        val heatmap = listOf(
            SalesHeatmapPoint(
                deviceId = "terminal-1001",
                location = Location(latitude = -33.8688, longitude = 151.2093),
                salesTotalCents = 123456,
            ),
        )
        `when`(deviceRepository.getSalesHeatmap(null, null, null, null)).thenReturn(heatmap)

        val response = controller.getSalesHeatmap(null, null, null, null)

        assertEquals(heatmap, response)
    }

    @Test
    fun `returns sales heatmap restricted to a bounding box`() {
        val deviceRepository = mock(DeviceRepository::class.java)
        val controller = SalesController(deviceRepository)

        val heatmap = listOf(
            SalesHeatmapPoint(
                deviceId = "terminal-1001",
                location = Location(latitude = -33.8688, longitude = 151.2093),
                salesTotalCents = 123456,
            ),
        )
        `when`(deviceRepository.getSalesHeatmap(-34.0, 151.0, -33.0, 152.0)).thenReturn(heatmap)

        val response = controller.getSalesHeatmap(minLat = -34.0, minLon = 151.0, maxLat = -33.0, maxLon = 152.0)

        assertEquals(heatmap, response)
    }
}
