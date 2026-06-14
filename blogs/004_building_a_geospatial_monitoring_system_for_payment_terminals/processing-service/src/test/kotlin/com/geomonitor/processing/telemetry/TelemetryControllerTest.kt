package com.geomonitor.processing.telemetry

import com.geomonitor.processing.cache.LocationCache
import com.geomonitor.processing.geolocation.GeolocationClient
import com.geomonitor.processing.model.CellTower
import com.geomonitor.processing.model.DeviceTelemetry
import com.geomonitor.processing.model.Location
import com.geomonitor.processing.model.WifiAccessPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions

class TelemetryControllerTest {

    @Test
    fun `device-reported gps location overrides cache and provider resolution`() {
        val locationCache = mock(LocationCache::class.java)
        val geolocationClient = mock(GeolocationClient::class.java)
        val controller = TelemetryController(locationCache, geolocationClient)

        val gps = Location(latitude = -33.8688, longitude = 151.2093, accuracyMeters = 12.5)
        val tower = CellTower(mcc = 310, mnc = 410, lac = 5678, cellId = 1234567)
        val accessPoint = WifiAccessPoint(macAddress = "00:25:9c:cf:1c:ac")
        val telemetry = DeviceTelemetry(
            deviceId = "terminal-123",
            cellTowers = listOf(tower),
            wifiAccessPoints = listOf(accessPoint),
            gpsLocation = gps,
        )

        val response = controller.process(telemetry)

        assertEquals("terminal-123", response.deviceId)
        assertEquals(gps, response.location)
        assertEquals("gps", response.source)

        // The reported GPS fix is used to refine the cell tower and WiFi caches too.
        verify(locationCache).storeCellTowerLocation(tower, gps)
        verify(locationCache).storeWifiAccessPointLocation(accessPoint, gps)
        verify(locationCache).storeDeviceLocation("terminal-123", gps, "gps")
        verify(locationCache, never()).getDeviceLocation(anyString())
        verifyNoInteractions(geolocationClient)
    }

    @Test
    fun `returns cached device location when available`() {
        val locationCache = mock(LocationCache::class.java)
        val geolocationClient = mock(GeolocationClient::class.java)
        val controller = TelemetryController(locationCache, geolocationClient)

        val cached = Location(latitude = 1.0, longitude = 2.0)
        `when`(locationCache.getDeviceLocation("terminal-123")).thenReturn(cached)

        val response = controller.process(DeviceTelemetry(deviceId = "terminal-123"))

        assertEquals(cached, response.location)
        assertEquals("cache", response.source)
        verifyNoInteractions(geolocationClient)
    }

    @Test
    fun `falls back to cell tower cache when device location is unknown`() {
        val locationCache = mock(LocationCache::class.java)
        val geolocationClient = mock(GeolocationClient::class.java)
        val controller = TelemetryController(locationCache, geolocationClient)

        val tower = CellTower(mcc = 310, mnc = 410, lac = 5678, cellId = 1234567)
        val cached = Location(latitude = 1.0, longitude = 2.0)
        `when`(locationCache.getCellTowerLocation(tower)).thenReturn(cached)

        val response = controller.process(DeviceTelemetry(deviceId = "terminal-123", cellTowers = listOf(tower)))

        assertEquals(cached, response.location)
        assertEquals("cache", response.source)
        verifyNoInteractions(geolocationClient)
    }

    @Test
    fun `checks the connected wifi access point before other scanned access points`() {
        val locationCache = mock(LocationCache::class.java)
        val geolocationClient = mock(GeolocationClient::class.java)
        val controller = TelemetryController(locationCache, geolocationClient)

        val scanned = WifiAccessPoint(macAddress = "11:22:33:44:55:66")
        val connected = WifiAccessPoint(macAddress = "AA:BB:CC:DD:EE:FF", connected = true)
        val cached = Location(latitude = 1.0, longitude = 2.0)
        `when`(locationCache.getWifiAccessPointLocation(connected)).thenReturn(cached)

        val response = controller.process(
            DeviceTelemetry(deviceId = "terminal-123", wifiAccessPoints = listOf(scanned, connected)),
        )

        assertEquals(cached, response.location)
        assertEquals("cache", response.source)
        verify(locationCache, never()).getWifiAccessPointLocation(scanned)
    }

    @Test
    fun `falls back to the geolocation provider and caches the resolved location`() {
        val locationCache = mock(LocationCache::class.java)
        val geolocationClient = mock(GeolocationClient::class.java)
        val controller = TelemetryController(locationCache, geolocationClient)

        val tower = CellTower(mcc = 310, mnc = 410, lac = 5678, cellId = 1234567)
        val accessPoint = WifiAccessPoint(macAddress = "00:25:9c:cf:1c:ac")
        val resolved = Location(latitude = 3.0, longitude = 4.0, accuracyMeters = 1500.0)
        `when`(geolocationClient.providerName).thenReturn("stub")
        `when`(geolocationClient.resolve(listOf(tower), listOf(accessPoint))).thenReturn(resolved)

        val response = controller.process(
            DeviceTelemetry(deviceId = "terminal-123", cellTowers = listOf(tower), wifiAccessPoints = listOf(accessPoint)),
        )

        assertEquals(resolved, response.location)
        assertEquals("stub", response.source)
        verify(locationCache).storeCellTowerLocation(tower, resolved)
        verify(locationCache).storeWifiAccessPointLocation(accessPoint, resolved)
        verify(locationCache).storeDeviceLocation("terminal-123", resolved, "stub")
    }
}
