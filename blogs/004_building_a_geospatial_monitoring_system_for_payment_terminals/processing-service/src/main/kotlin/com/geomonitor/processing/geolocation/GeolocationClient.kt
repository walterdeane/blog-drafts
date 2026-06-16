package com.geomonitor.processing.geolocation

import com.geomonitor.processing.model.CellTower
import com.geomonitor.processing.model.Location
import com.geomonitor.processing.model.WifiAccessPoint

/**
 * Resolves a location from a set of cell tower and/or WiFi access point signals.
 *
 * Implementations are only consulted when no cached location is available - see the
 * caching strategy described in the blog post. The active implementation is selected
 * via the `app.geolocation.provider` property.
 */
interface GeolocationClient {
    /** Identifies this provider; recorded as the cache's `source` for resolved locations. */
    val providerName: String

    fun resolve(cellTowers: List<CellTower>, wifiAccessPoints: List<WifiAccessPoint>, radioType: String? = null): Location
}
