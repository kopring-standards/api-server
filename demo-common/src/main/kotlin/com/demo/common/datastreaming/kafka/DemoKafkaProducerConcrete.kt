package com.demo.common.datastreaming.kafka

import com.demo.common.datastreaming.kafka.event.DemoEventContent
import com.demo.common.datastreaming.kafka.event.DemoEventHeader
import com.demo.common.datastreaming.kafka.event.DemoEventKey
import com.demo.common.datastreaming.kafka.event.DemoEventTopic
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.lang.Thread.currentThread
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

@Component
class DemoKafkaProducerConcrete(
    private val kafkaTemplate: KafkaTemplate<String, DemoEventContent>,
    private val eventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
    @Value("\${spring.application.name:demo-app}") private val applicationName: String,
) : DemoKafkaProducer {
    private val transactionId: String = generateTransactionId()

    init {
        if (kafkaTemplate.producerFactory is DefaultKafkaProducerFactory) {
            (kafkaTemplate.producerFactory as DefaultKafkaProducerFactory<*, *>).setTransactionIdPrefix(transactionId)
        }
    }

    override fun execute(
        demoEventTopic: DemoEventTopic,
        demoEventContent: DemoEventContent,
        demoEventHeader: DemoEventHeader?,
        demoEventKey: DemoEventKey?,
    ) {
        publishEvent(
            topic = demoEventTopic,
            content = demoEventContent,
            header = demoEventHeader,
            partitionKey = demoEventKey,
            isAsync = false,
        )
    }

    override fun executeAsync(
        demoEventTopic: DemoEventTopic,
        demoEventContent: DemoEventContent,
        demoEventHeader: DemoEventHeader?,
        demoEventKey: DemoEventKey?,
    ) {
        publishEvent(
            topic = demoEventTopic,
            content = demoEventContent,
            header = demoEventHeader,
            partitionKey = demoEventKey,
            isAsync = true,
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleSyncKafkaEvent(event: KafkaEvent) {
        if (!event.isAsync) {
            kafkaTemplate.executeInTransaction<KafkaOperations<String, DemoEventContent>> { operations ->
                try {
                    log.info("동기 메시지 발행 처리 - Thread: {}", currentThread())
                    val sendOperation = createAndSendRecord(operations, event)
                    processSendResult(sendOperation, event, isAsync = false)
                    operations
                } catch (ex: Exception) {
                    logSendFailure(ex, event)
                    throw RuntimeException("예기치 못한 이유로 카프카 메시지 발행에 실패했습니다.", ex)
                }
            }
        }
    }

    @EventListener
    fun handleAsyncKafkaEvent(event: KafkaEvent) {
        if (event.isAsync) {
            try {
                log.info("비동기 메시지 발행 처리 - Thread: {}", currentThread())
                val sendOperation = createAndSendRecord(kafkaTemplate, event)
                processSendResult(sendOperation, event, isAsync = true)
            } catch (ex: Exception) {
                logSendFailure(ex, event)
            }
        }
    }

    private fun publishEvent(
        topic: DemoEventTopic,
        content: DemoEventContent,
        header: DemoEventHeader?,
        partitionKey: DemoEventKey?,
        isAsync: Boolean,
    ) {
        val kafkaEvent =
            KafkaEvent(
                topic = topic,
                content = content,
                header = header,
                partitionKey = partitionKey,
                isAsync = isAsync,
            )
        eventPublisher.publishEvent(kafkaEvent)
    }

    private fun createAndSendRecord(
        operations: KafkaOperations<String, DemoEventContent>,
        event: KafkaEvent,
    ): CompletableFuture<SendResult<String, DemoEventContent>> =
        when {
            event.partitionKey != null && event.header != null -> {
                val record = ProducerRecord(event.topic.name, event.partitionKey.name, event.content)
                addHeader(record, event.header)
                operations.send(record)
            }

            event.partitionKey != null -> operations.send(event.topic.name, event.partitionKey.name, event.content)
            event.header != null -> {
                val record = ProducerRecord<String, DemoEventContent>(event.topic.name, null, event.content)
                addHeader(record, event.header)
                operations.send(record)
            }

            else -> operations.send(event.topic.name, event.content)
        }

    private fun processSendResult(
        sendOperation: CompletableFuture<SendResult<String, DemoEventContent>>,
        event: KafkaEvent,
        isAsync: Boolean,
    ) {
        if (isAsync) {
            sendOperation.whenComplete { result, ex ->
                if (ex != null) {
                    logSendFailure(ex, event)
                } else {
                    logSendSuccess(result)
                }
            }
        } else {
            try {
                val result = sendOperation.join()
                logSendSuccess(result)
            } catch (ex: Exception) {
                logSendFailure(ex, event)
                throw RuntimeException("예기치 못한 이유로 카프카 메시지 발행에 실패했습니다.", ex)
            }
        }
    }

    private fun addHeader(
        record: ProducerRecord<String, DemoEventContent>,
        header: DemoEventHeader,
    ) {
        try {
            val jsonHeader = objectMapper.writeValueAsString(header)
            record.headers().add(
                RecordHeader("metadata", jsonHeader.toByteArray(StandardCharsets.UTF_8)),
            )
        } catch (ex: Exception) {
            log.error("헤더 직렬화 실패: {}", ex.message, ex)
            throw RuntimeException("헤더 직렬화 중 오류가 발생했습니다.", ex)
        }
    }

    private fun generateTransactionId(): String =
        try {
            val hostName = InetAddress.getLocalHost().hostName
            "$applicationName-$hostName-producer-1"
        } catch (ex: UnknownHostException) {
            log.warn("호스트 이름을 가져오는데 실패했습니다. 기본값을 사용합니다.", ex)
            "$applicationName-unknown-host-producer-1"
        }

    private fun logSendSuccess(result: SendResult<String, DemoEventContent>) {
        log.info(
            """
            카프카 메시지 전송 성공
            토픽: {}
            파티션: {}
            오프셋: {}
            """.trimIndent(),
            result.recordMetadata.topic(),
            result.recordMetadata.partition(),
            result.recordMetadata.offset(),
        )
    }

    private fun logSendFailure(
        ex: Throwable,
        event: KafkaEvent,
    ) {
        log.error(
            """
            카프카 메시지 전송 실패
            토픽 정보
              이름: {}
              파티션키: {}
            메시지 정보
              컨텐츠: {}
              헤더: {}
              비동기 처리: {}
            에러 정보
              종류: {}
              메시지: {}
            """.trimIndent(),
            event.topic.name,
            event.partitionKey?.name,
            event.content,
            event.header,
            event.isAsync,
            ex.javaClass.simpleName,
            ex.message,
            ex,
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(DemoKafkaProducerConcrete::class.java)
    }
}

/**
 * KafkaEvent 클래스는 메시지 발행에 필요한 정보와 발행 방식(동기/비동기)을 포함합니다.
 */
data class KafkaEvent(
    val topic: DemoEventTopic,
    val content: DemoEventContent,
    val header: DemoEventHeader?,
    val partitionKey: DemoEventKey?,
    val isAsync: Boolean,
)
