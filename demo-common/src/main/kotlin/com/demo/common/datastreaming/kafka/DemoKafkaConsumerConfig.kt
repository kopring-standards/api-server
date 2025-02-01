package com.demo.common.datastreaming.kafka

import com.demo.common.datastreaming.kafka.event.DemoEventContent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.listener.ContainerProperties

@Configuration
class DemoKafkaConsumerConfig(
    private val kafkaProperties: KafkaProperties,
    private val consumerFactory: ConsumerFactory<String, DemoEventContent>,
) {
    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, DemoEventContent> {
        val factory: ConcurrentKafkaListenerContainerFactory<String, DemoEventContent> =
            ConcurrentKafkaListenerContainerFactory<String, DemoEventContent>()
        factory.consumerFactory = consumerFactory
        factory.setAutoStartup(kafkaProperties.consumer.autoStartup)
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
//        factory.setCommonErrorHandler(errorHandler)
        return factory
    }
}
