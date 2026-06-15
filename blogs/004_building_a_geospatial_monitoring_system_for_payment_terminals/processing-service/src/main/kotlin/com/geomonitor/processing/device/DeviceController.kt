package com.geomonitor.processing.device

import com.geomonitor.processing.model.DeviceDetail
import com.geomonitor.processing.model.DeviceSummary
import com.geomonitor.processing.model.DeviceTelemetryAttributes
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/devices")
class DeviceController(private val deviceRepository: DeviceRepository) {

    @GetMapping
    fun listDevices(): List<DeviceSummary> = deviceRepository.listDevices()

    @GetMapping("/{deviceId}")
    fun getDevice(@PathVariable deviceId: String): ResponseEntity<DeviceDetail> =
        deviceRepository.getDevice(deviceId)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @PutMapping("/{deviceId}/telemetry")
    fun updateTelemetry(
        @PathVariable deviceId: String,
        @RequestBody attributes: DeviceTelemetryAttributes,
    ): ResponseEntity<DeviceDetail> {
        if (!deviceRepository.updateTelemetry(deviceId, attributes)) {
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.ok(deviceRepository.getDevice(deviceId))
    }
}
