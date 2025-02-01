package com.demo.common.datastreaming.kafka

import com.demo.common.datastreaming.kafka.event.DemoEventContent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer

@Configuration
class KafkaConsumerErrorHandlingConfig(
    private val kafkaProperties: KafkaProperties,
    private val kafkaTemplate: KafkaTemplate<String, DemoEventContent>,
) {
    @Bean
    fun deadLetterPublishingRecoverer(): DeadLetterPublishingRecoverer {
        return DeadLetterPublishingRecoverer(
            kafkaTemplate,
        ) { record: ConsumerRecord<*, *>, _: Exception ->
            createDeadLetterTopicPartition(record)
        }
    }

    @Bean
    fun errorHandler(): org.springframework.kafka.listener.DefaultErrorHandler {
        // 재시도 간격 (ms) 및 최대 재시도 횟수 설정
        val backOff =
            org.springframework.util.backoff.FixedBackOff(
                kafkaProperties.errorHandler.retryInterval.toLong(),
                kafkaProperties.errorHandler.maxRetryAttempts.toLong(),
            )
        val errorHandler =
            org.springframework.kafka.listener.DefaultErrorHandler(deadLetterPublishingRecoverer(), backOff)
        errorHandler.setCommitRecovered(true)

        errorHandler.setLogLevel(org.springframework.kafka.KafkaException.Level.ERROR)

        return errorHandler
    }

    private fun createDeadLetterTopicPartition(record: ConsumerRecord<*, *>): TopicPartition {
        return TopicPartition(
            record.topic() + kafkaProperties.errorHandler.dltPostfix,
            record.partition(),
        )
    }
}
