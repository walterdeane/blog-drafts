package com.geomonitor.processing.geolocation

import com.geomonitor.processing.model.CellTower
import com.geomonitor.processing.model.Location
import com.geomonitor.processing.model.WifiAccessPoint
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * Deterministic stand-in for [GoogleGeolocationClient], used by default so the demo can
 * run without a Google API key.
 *
 * Derives a plausible-looking lat/lng/accuracy from the first cell tower's identifiers,
 * or the first WiFi access point's BSSID if no cell tower is present, so the same
 * signal always resolves to the same location.
 */
@Component
@ConditionalOnProperty(name = ["app.geolocation.provider"], havingValue = "stub", matchIfMissing = true)
class StubGeolocationClient : GeolocationClient {
    override val providerName = "stub"

    override fun resolve(cellTowers: List<CellTower>, wifiAccessPoints: List<WifiAccessPoint>, radioType: String?): Location {
        cellTowers.firstOrNull()?.let { tower ->
            val latitude = ((tower.mcc * 31 + tower.lac) % 18000) / 100.0 - 90.0
            val longitude = ((tower.mnc * 31 + tower.cellId) % 36000) / 100.0 - 180.0
            val accuracyMeters = ((tower.lac * 31 + tower.cellId) % 4500 + 500).toDouble()
            return Location(latitude = latitude, longitude = longitude, accuracyMeters = accuracyMeters)
        }

        val accessPoint = wifiAccessPoints.firstOrNull()
            ?: error("Resolving a location requires at least one cell tower or WiFi access point")

        val hash = accessPoint.macAddress.replace(":", "").replace("-", "").lowercase().toLong(16)
        val latitude = (hash % 18000) / 100.0 - 90.0
        val longitude = ((hash / 18000) % 36000) / 100.0 - 180.0
        val accuracyMeters = (hash % 4500 + 500).toDouble()
        return Location(latitude = latitude, longitude = longitude, accuracyMeters = accuracyMeters)
    }
}
