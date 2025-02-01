package com.demo.common.datastreaming.kafka

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kafka")
data class KafkaProperties(val consumer: ConsumerConfig, val errorHandler: ErrorHandlerConfig, val dlt: DltConfig) {
    data class ConsumerConfig(val autoStartup: Boolean, val staticInstanceId: String)

    data class ErrorHandlerConfig(val dltPostfix: String, val retryInterval: Int, val maxRetryAttempts: Int)

    data class DltConfig(val administrators: Array<String>, val ui: UiConfig)

    data class UiConfig(val prod: ProdConfig, val dev: DevConfig, val path: String) {
        data class ProdConfig(val baseUrl: String)

        data class DevConfig(val baseUrl: String)
    }
}
