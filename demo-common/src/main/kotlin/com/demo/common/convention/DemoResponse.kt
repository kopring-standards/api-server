package com.demo.common.convention

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.http.ResponseEntity

@JsonIgnoreProperties(ignoreUnknown = true)
data class ResponseForm<T>(
    val data: T,
    val timestamp: Long = System.currentTimeMillis(),
) {
    companion object {
        fun <T> ok(data: T): ResponseEntity<ResponseForm<T>> = ResponseEntity.ok(ResponseForm(data))

        fun <T> created(data: T): ResponseEntity<ResponseForm<T>> = ResponseEntity.status(201).body(ResponseForm(data))
    }
}

data class ExceptionForm(
    val path: String?,
    val statusCode: Int,
    val category: ExceptionCategory,
    val errorCode: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: Map<String, Any>? = null,
)

class DemoException(
    private val exceptionCode: ExceptionCode,
    private val path: String?,
    private val messageArgs: Array<String> = arrayOf(),
    private val details: Map<String, Any>? = null,
    cause: Throwable? = null,
) : RuntimeException(exceptionCode.name, cause) {
    fun toExceptionForm(): ExceptionForm {
        val formattedMessage =
            if (messageArgs.isEmpty()) {
                exceptionCode.messageTemplate
            } else {
                messageArgs.foldIndexed(exceptionCode.messageTemplate) { index, acc, arg ->
                    acc.replace("{$index}", arg)
                }
            }

        return ExceptionForm(
            path = path,
            statusCode = exceptionCode.statusCode,
            category = exceptionCode.exceptionCategory,
            errorCode = exceptionCode.name,
            message = formattedMessage,
            details = details,
        )
    }
}
