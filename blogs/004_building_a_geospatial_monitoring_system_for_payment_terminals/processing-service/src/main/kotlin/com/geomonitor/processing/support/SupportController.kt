package com.geomonitor.processing.support

import com.geomonitor.processing.device.DeviceRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException

/**
 * Server-rendered support pages for the demo: a device list/detail view with an
 * editable telemetry form, an LWT-style connectivity simulator, and a location
 * telemetry simulator. Each page's form posts to the corresponding `/api/...` or
 * `/telemetry` REST endpoint via a small inline `fetch()` script.
 */
@Controller
@RequestMapping("/support")
class SupportController(private val deviceRepository: DeviceRepository) {

    @GetMapping
    fun listDevices(model: Model): String {
        model.addAttribute("devices", deviceRepository.listDevices())
        return "devices/list"
    }

    @GetMapping("/devices/{deviceId}")
    fun deviceDetail(@PathVariable deviceId: String, model: Model): String {
        val device = deviceRepository.getDevice(deviceId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown device: $deviceId")

        model.addAttribute("device", device)
        model.addAttribute("connectivityHistory", deviceRepository.getConnectivityHistory(deviceId))
        model.addAttribute("salesSummary", deviceRepository.getSalesSummary(deviceId))
        model.addAttribute("networkInterfaces", NETWORK_INTERFACES)
        model.addAttribute("networkProviders", NETWORK_PROVIDERS)
        model.addAttribute("merchantCategories", MERCHANT_CATEGORIES)
        return "devices/detail"
    }

    @GetMapping("/connectivity")
    fun connectivitySimulator(model: Model): String {
        model.addAttribute("devices", deviceRepository.listDevices())
        model.addAttribute("statuses", CONNECTIVITY_STATUSES)
        return "connectivity"
    }

    @GetMapping("/location-telemetry")
    fun locationTelemetrySimulator(model: Model): String {
        model.addAttribute("devices", deviceRepository.listDevices())
        model.addAttribute("sampleTelemetry", SAMPLE_TELEMETRY_JSON)
        return "location-telemetry"
    }

    companion object {
        val NETWORK_INTERFACES = listOf("WIFI", "MOBILE", "ETHERNET")
        val NETWORK_PROVIDERS = listOf("Telstra", "Optus", "Vodafone", "TPG")
        val CONNECTIVITY_STATUSES = listOf("online", "expected_offline", "unexpected_offline")

        /** Mirrors the merchant categories used by the demo seed data. */
        val MERCHANT_CATEGORIES = listOf(
            5814 to "Fast Food Restaurant",
            5812 to "Restaurant",
            5499 to "Convenience Store",
            5411 to "Grocery Store",
            5651 to "Clothing Store",
            5912 to "Pharmacy",
            5541 to "Service Station",
            5311 to "Department Store",
            5942 to "Book Store",
            5944 to "Jewelry Store",
        )

        private val SAMPLE_TELEMETRY_JSON = """
            {
              "device_id": "terminal-1001",
              "cell_towers": [
                {"mcc": 505, "mnc": 1, "lac": 5678, "cell_id": 1234567}
              ],
              "wifi_access_points": [
                {"mac_address": "00:25:9c:cf:1c:ac", "signal_strength": -55, "connected": true}
              ],
              "gps_location": {"latitude": -33.8688, "longitude": 151.2093, "accuracyMeters": 12.5}
            }
        """.trimIndent()
    }
}
