package com.gaspayer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GasPayerServiceApplication

fun main(args: Array<String>) {
    runApplication<GasPayerServiceApplication>(*args)
}