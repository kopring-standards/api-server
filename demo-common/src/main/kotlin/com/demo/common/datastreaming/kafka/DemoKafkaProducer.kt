package com.demo.common.datastreaming.kafka

import com.demo.common.datastreaming.kafka.event.DemoEventContent
import com.demo.common.datastreaming.kafka.event.DemoEventHeader
import com.demo.common.datastreaming.kafka.event.DemoEventKey
import com.demo.common.datastreaming.kafka.event.DemoEventTopic

interface DemoKafkaProducer {
    fun execute(
        demoEventTopic: DemoEventTopic,
        demoEventContent: DemoEventContent,
        demoEventHeader: DemoEventHeader?,
        demoEventKey: DemoEventKey?,
    )

    fun executeAsync(
        demoEventTopic: DemoEventTopic,
        demoEventContent: DemoEventContent,
        demoEventHeader: DemoEventHeader?,
        demoEventKey: DemoEventKey?,
    )
}
