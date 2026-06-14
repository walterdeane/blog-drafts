package com.geomonitor.processing.geolocation

import com.fasterxml.jackson.annotation.JsonInclude
import com.geomonitor.processing.model.CellTower
import com.geomonitor.processing.model.Location
import com.geomonitor.processing.model.WifiAccessPoint
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

private const val GOOGLE_GEOLOCATION_URL = "https://www.googleapis.com/geolocation/v1/geolocate"

/**
 * Thin wrapper around the Google Geolocation API.
 *
 * This should only be called when no cached location is available -
 * see the caching strategy described in the blog post.
 */
@Component
@ConditionalOnProperty(name = ["app.geolocation.provider"], havingValue = "google")
class GoogleGeolocationClient(
    @Value("\${app.google.geolocation-api-key}") private val apiKey: String,
    restClientBuilder: RestClient.Builder,
) : GeolocationClient {
    override val providerName = "google"

    private val restClient = restClientBuilder.baseUrl(GOOGLE_GEOLOCATION_URL).build()

    override fun resolve(cellTowers: List<CellTower>, wifiAccessPoints: List<WifiAccessPoint>): Location {
        val payload = GeolocationRequest(
            considerIp = false,
            cellTowers = cellTowers.map {
                GeolocationRequest.CellTowerSignal(
                    mobileCountryCode = it.mcc,
                    mobileNetworkCode = it.mnc,
                    locationAreaCode = it.lac,
                    cellId = it.cellId,
                )
            },
            wifiAccessPoints = wifiAccessPoints.map {
                GeolocationRequest.WifiAccessPointSignal(
                    macAddress = it.macAddress,
                    signalStrength = it.signalStrengthDbm,
                )
            },
        )

        val response = restClient.post()
            .uri { it.queryParam("key", apiKey).build() }
            .contentType(MediaType.APPLICATION_JSON)
            .body(payload)
            .retrieve()
            .body<GeolocationResponse>()
            ?: error("Empty response from Google Geolocation API")

        return Location(
            latitude = response.location.lat,
            longitude = response.location.lng,
            accuracyMeters = response.accuracy,
        )
    }
}

data class GeolocationRequest(
    val considerIp: Boolean,
    val cellTowers: List<CellTowerSignal>,
    val wifiAccessPoints: List<WifiAccessPointSignal>,
) {
    data class CellTowerSignal(
        val mobileCountryCode: Int,
        val mobileNetworkCode: Int,
        val locationAreaCode: Int,
        val cellId: Int,
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class WifiAccessPointSignal(
        val macAddress: String,
        val signalStrength: Int? = null,
    )
}

data class GeolocationResponse(val location: Coordinates, val accuracy: Double) {
    data class Coordinates(val lat: Double, val lng: Double)
}
