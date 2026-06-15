package com.geomonitor.processing.device

import com.geomonitor.processing.model.SalesHeatmapPoint
import com.geomonitor.processing.model.SalesSummaryEntry
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class SalesController(private val deviceRepository: DeviceRepository) {

    @GetMapping("/devices/{deviceId}/sales")
    fun getSalesSummary(@PathVariable deviceId: String): List<SalesSummaryEntry> =
        deviceRepository.getSalesSummary(deviceId)

    /**
     * Total sales per device over its summary history, optionally restricted to a
     * `minLat,minLon,maxLat,maxLon` bounding box - a sample geospatial query for
     * heatmap-style visualisation.
     */
    @GetMapping("/sales/heatmap")
    fun getSalesHeatmap(
        @RequestParam(required = false) minLat: Double?,
        @RequestParam(required = false) minLon: Double?,
        @RequestParam(required = false) maxLat: Double?,
        @RequestParam(required = false) maxLon: Double?,
    ): List<SalesHeatmapPoint> =
        deviceRepository.getSalesHeatmap(minLat, minLon, maxLat, maxLon)
}
