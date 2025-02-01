package com.demo.common.datastreaming.kafka.event

interface DemoEventTopic {
    val name: String
}

data class TestTopic(
    override val name: String = "test",
) : DemoEventTopic
