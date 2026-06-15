package com.geomonitor.processing.demo

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Demo map pages: Leaflet maps backed by GeoServer WMS layers, with legends and
 * layer toggles, as a more polished alternative to linking straight to the
 * GeoServer Layer Preview.
 */
@Controller
@RequestMapping("/demo")
class DemoMapController(
    @Value("\${app.geoserver.url:http://localhost:8080/geoserver}") private val geoserverUrl: String,
) {

    @GetMapping("/terminal-status")
    fun terminalStatus(model: Model): String {
        model.addAttribute("wmsUrl", wmsUrl())
        return "demo/terminal-status"
    }

    @GetMapping("/terminal-network")
    fun terminalNetwork(model: Model): String {
        model.addAttribute("wmsUrl", wmsUrl())
        return "demo/terminal-network"
    }

    private fun wmsUrl(): String = "$geoserverUrl/geospatial-monitoring/wms"
}
