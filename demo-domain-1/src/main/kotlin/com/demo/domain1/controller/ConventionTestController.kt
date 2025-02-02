package com.demo.domain1.controller

import com.demo.common.convention.DemoException
import com.demo.common.convention.ExceptionCode
import com.demo.common.convention.ResponseForm
import com.demo.domain1.controller.dto.SuccessMockData
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/convention")
@Transactional
class ConventionTestController {
    @GetMapping("/success")
    fun testSuccess(): ResponseEntity<ResponseForm<SuccessMockData>> {
        return ResponseForm.ok(
            SuccessMockData(
                id = 1L,
                name = "test",
            ),
        )
    }

    @GetMapping("/created")
    fun testCreated(): ResponseEntity<ResponseForm<SuccessMockData>> {
        return ResponseForm.created(
            SuccessMockData(
                id = 1L,
                name = "test",
            ),
        )
    }

    @GetMapping("/bad-request")
    fun testInvalidParameter(): Nothing {
        throw DemoException(
            exceptionCode = ExceptionCode.INVALID_PARAMETER,
            path = "/convention/bad-request/invalid-param",
            messageArgs = arrayOf("사용자 ID는 양의 정수여야 합니다"),
            details =
                mapOf(
                    "field" to "userId",
                    "invalidValue" to "-1",
                    "allowedValues" to "1 이상의 정수",
                ),
        )
    }
}
