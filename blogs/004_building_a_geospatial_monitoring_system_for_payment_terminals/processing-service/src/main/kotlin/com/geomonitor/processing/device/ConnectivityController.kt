package com.geomonitor.processing.device

import com.geomonitor.processing.model.ConnectivityEvent
import com.geomonitor.processing.model.ConnectivityStatusRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Simulates LWT-style ("last will and testament") connectivity messages for a
 * device: online, expected_offline (clean disconnect/shutdown), or
 * unexpected_offline (broker-detected drop). Each message updates the device's
 * current connectivity status and is appended to its audit history.
 */
@RestController
@RequestMapping("/api/devices/{deviceId}/connectivity")
class ConnectivityController(private val deviceRepository: DeviceRepository) {

    @PostMapping
    fun recordConnectivityEvent(
        @PathVariable deviceId: String,
        @RequestBody request: ConnectivityStatusRequest,
    ): ResponseEntity<ConnectivityEvent> =
        deviceRepository.recordConnectivityEvent(deviceId, request.status)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @GetMapping
    fun getConnectivityHistory(@PathVariable deviceId: String): List<ConnectivityEvent> =
        deviceRepository.getConnectivityHistory(deviceId)
}
