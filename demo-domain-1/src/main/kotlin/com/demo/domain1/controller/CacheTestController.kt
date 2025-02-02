package com.demo.domain1.controller

import com.demo.common.convention.ResponseForm
import com.demo.domain1.controller.dto.SuccessMockData
import com.demo.domain1.service.Domain1Service
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/cache")
@Transactional
class CacheTestController(
    private val domain1Service: Domain1Service,
) {
    @GetMapping("/static")
    fun testStaticCache(
        @RequestParam id: Long,
        @RequestParam name: String,
    ): ResponseEntity<ResponseForm<SuccessMockData>> {
        return ResponseForm.ok(domain1Service.getStaticData(id, name))
    }
}
