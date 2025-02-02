package com.demo.domain1.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class SuccessMockData(
    @JsonProperty("id") val id: Long,
    @JsonProperty("name") val name: String,
)
