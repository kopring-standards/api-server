package com.demo.common.datastreaming.kafka.event

interface DemoEventKey {
    val name: String
}

data class TestEventKey(
    override val name: String = "test",
) : DemoEventKey
