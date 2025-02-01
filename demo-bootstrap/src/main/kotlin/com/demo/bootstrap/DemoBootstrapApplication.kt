package com.demo.bootstrap

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.demo"])
class DemoBootstrapApplication

fun main(args: Array<String>) {
    runApplication<DemoBootstrapApplication>(*args)
}
