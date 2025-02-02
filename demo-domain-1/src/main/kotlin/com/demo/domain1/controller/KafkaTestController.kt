package com.demo.domain1.controller

import com.demo.common.datastreaming.kafka.DemoKafkaProducer
import com.demo.common.datastreaming.kafka.event.DemoEventContent
import com.demo.common.datastreaming.kafka.event.DemoEventHeader
import com.demo.common.datastreaming.kafka.event.TestEventKey
import com.demo.common.datastreaming.kafka.event.TestTopic
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/kafka")
@Transactional
class KafkaTestController(
    private val demoKafkaProducer: DemoKafkaProducer,
) {
    // 기본 메시지 발행 테스트 (키와 헤더 없음)
    @GetMapping("/basic")
    fun testBasicMessage() {
        demoKafkaProducer.execute(
            demoEventTopic = TestTopic(),
            demoEventContent = TestContent(),
            demoEventHeader = null,
            demoEventKey = null,
        )
    }

    // 메시지 키를 포함한 발행 테스트
    @GetMapping("/with-key")
    fun testMessageWithKey() {
        demoKafkaProducer.execute(
            demoEventTopic = TestTopic(),
            demoEventContent = TestContent(),
            demoEventHeader = null,
            demoEventKey = TestEventKey("test-key"),
        )
    }

    // 헤더를 포함한 발행 테스트
    @GetMapping("/with-header")
    fun testMessageWithHeader() {
        demoKafkaProducer.execute(
            demoEventTopic = TestTopic(),
            demoEventContent = TestContent(),
            demoEventHeader = TestHeader(),
            demoEventKey = null,
        )
    }

    // 키와 헤더 모두 포함한 발행 테스트
    @GetMapping("/with-key-and-header")
    fun testMessageWithKeyAndHeader() {
        demoKafkaProducer.execute(
            demoEventTopic = TestTopic(),
            demoEventContent = TestContent(),
            demoEventHeader = TestHeader(),
            demoEventKey = TestEventKey("test-key"),
        )
    }

    @GetMapping("/async")
    fun testAsyncMessage() {
        demoKafkaProducer.executeAsync(
            demoEventTopic = TestTopic(),
            demoEventContent = TestContent(),
            demoEventHeader = TestHeader(),
            demoEventKey = TestEventKey("async-test"),
        )
    }
}

// 테스트용 컨텐츠 클래스
data class TestContent(
    val message: String = "테스트 메시지",
    val timestamp: Long = System.currentTimeMillis(),
) : DemoEventContent

// 테스트용 헤더 클래스
data class TestHeader(
    val version: String = "1.0",
    val messageId: String = UUID.randomUUID().toString(),
) : DemoEventHeader
