package com.demo.common.datastreaming.kafka

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka

@EnableKafka
@Configuration
@EnableConfigurationProperties(KafkaProperties::class)
class DemoKafkaConfig
