package com.geomonitor.processing.geolocation

import com.geomonitor.processing.model.CellTower
import com.geomonitor.processing.model.Location
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * Deterministic stand-in for [GoogleGeolocationClient], used by default so the demo can
 * run without a Google API key.
 *
 * Derives a plausible-looking lat/lng/accuracy from the first cell tower's identifiers
 * so the same tower always resolves to the same location.
 */
@Component
@ConditionalOnProperty(name = ["app.geolocation.provider"], havingValue = "stub", matchIfMissing = true)
class StubGeolocationClient : GeolocationClient {
    override val providerName = "stub"

    override fun resolve(cellTowers: List<CellTower>): Location {
        val tower = cellTowers.first()
        val latitude = ((tower.mcc * 31 + tower.lac) % 18000) / 100.0 - 90.0
        val longitude = ((tower.mnc * 31 + tower.cellId) % 36000) / 100.0 - 180.0
        val accuracyMeters = ((tower.lac * 31 + tower.cellId) % 4500 + 500).toDouble()
        return Location(latitude = latitude, longitude = longitude, accuracyMeters = accuracyMeters)
    }
}
