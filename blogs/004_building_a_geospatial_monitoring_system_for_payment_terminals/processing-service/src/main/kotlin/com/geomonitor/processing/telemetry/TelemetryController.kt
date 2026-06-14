package com.geomonitor.processing.telemetry

import com.fasterxml.jackson.annotation.JsonProperty
import com.geomonitor.processing.cache.LocationCache
import com.geomonitor.processing.geolocation.GeolocationClient
import com.geomonitor.processing.model.DeviceTelemetry
import com.geomonitor.processing.model.Location
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Accepts device telemetry as JSON and resolves each device's location using the
 * caching strategy from the blog post:
 *
 * 0. If the device reports its own GPS/fused location, trust it directly - it's more
 *    precise than any cached or API-derived estimate - and use it to refine the WiFi
 *    access point cache for the APs reported alongside it. A cell tower's coverage
 *    area is much larger than GPS accuracy, so a single device's fix isn't a good
 *    estimate of "the cell tower's location" for other devices and is not used to
 *    update the cell tower cache.
 * 1. Check whether the device already has a known location.
 * 2. Check the cell tower cache for the device's current tower(s).
 * 3. Check the WiFi access point cache, prioritizing the AP the device is currently
 *    connected/associated with, then falling back to the other scanned BSSIDs.
 * 4. Fall back to the Google Geolocation API only if none of the above are cached,
 *    then cache the result against every reported cell tower and access point.
 */
@RestController
@RequestMapping("/telemetry")
class TelemetryController(
    private val locationCache: LocationCache,
    private val geolocationClient: GeolocationClient,
) {
    private val logger = LoggerFactory.getLogger(TelemetryController::class.java)

    @PostMapping
    fun process(@RequestBody telemetry: DeviceTelemetry): ResolvedLocationResponse {
        val (location, source) = resolveLocation(telemetry)
        locationCache.storeDeviceLocation(telemetry.deviceId, location, source)
        logger.info("Resolved location for device {} from {}", telemetry.deviceId, source)
        return ResolvedLocationResponse(deviceId = telemetry.deviceId, location = location, source = source)
    }

    private fun resolveLocation(telemetry: DeviceTelemetry): Pair<Location, String> {
        telemetry.gpsLocation?.let { gpsLocation ->
            for (accessPoint in telemetry.wifiAccessPoints) {
                locationCache.storeWifiAccessPointLocation(accessPoint, gpsLocation)
            }
            return gpsLocation to "gps"
        }

        locationCache.getDeviceLocation(telemetry.deviceId)?.let { return it to "cache" }

        for (tower in telemetry.cellTowers) {
            locationCache.getCellTowerLocation(tower)?.let { return it to "cache" }
        }

        val (connected, scanned) = telemetry.wifiAccessPoints.partition { it.connected }
        for (accessPoint in connected + scanned) {
            locationCache.getWifiAccessPointLocation(accessPoint)?.let { return it to "cache" }
        }

        val location = geolocationClient.resolve(telemetry.cellTowers, telemetry.wifiAccessPoints)
        for (tower in telemetry.cellTowers) {
            locationCache.storeCellTowerLocation(tower, location)
        }
        for (accessPoint in telemetry.wifiAccessPoints) {
            locationCache.storeWifiAccessPointLocation(accessPoint, location)
        }
        return location to geolocationClient.providerName
    }
}

data class ResolvedLocationResponse(
    @JsonProperty("device_id") val deviceId: String,
    val location: Location,
    val source: String,
)
