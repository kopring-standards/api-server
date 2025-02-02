package com.demo.common.convention

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class DemoExceptionHandler {
    @ExceptionHandler(DemoException::class)
    fun handleDemoException(demoException: DemoException): ResponseEntity<ResponseForm<ExceptionForm>> {
        val errorResponse = demoException.toExceptionForm()
        return ResponseEntity
            .status(errorResponse.statusCode)
            .body(ResponseForm(errorResponse))
    }
}
