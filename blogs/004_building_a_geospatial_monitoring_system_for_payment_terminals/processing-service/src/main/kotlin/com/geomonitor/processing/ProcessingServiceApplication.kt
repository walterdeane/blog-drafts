package com.geomonitor.processing

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ProcessingServiceApplication

fun main(args: Array<String>) {
    runApplication<ProcessingServiceApplication>(*args)
}
