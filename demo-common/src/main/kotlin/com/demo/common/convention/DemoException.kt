package com.demo.common.convention

enum class ExceptionCategory {
    VALIDATION,
    AUTHENTICATION,
    AUTHORIZATION,
    BUSINESS,
    SYSTEM,
}

enum class ExceptionCode(
    val statusCode: Int,
    val exceptionCategory: ExceptionCategory,
    val messageTemplate: String,
) {
    // 400 Bad Request
    INVALID_PARAMETER(400, ExceptionCategory.VALIDATION, "잘못된 파라미터입니다: {0}"),
    INVALID_FORMAT(400, ExceptionCategory.VALIDATION, "잘못된 형식입니다: {0}"),
    MISSING_PARAMETER(400, ExceptionCategory.VALIDATION, "필수 파라미터가 누락되었습니다: {0}"),

    // 401 Unauthorized
    INVALID_TOKEN(401, ExceptionCategory.AUTHENTICATION, "유효하지 않은 토큰입니다: {0}"),
    EXPIRED_TOKEN(401, ExceptionCategory.AUTHENTICATION, "만료된 토큰입니다"),

    // 403 Forbidden
    FORBIDDEN_ACCESS(403, ExceptionCategory.AUTHORIZATION, "접근 권한이 없습니다: {0}"),

    // 404 Not Found
    RESOURCE_NOT_FOUND(404, ExceptionCategory.BUSINESS, "리소스를 찾을 수 없습니다: {0}"),

    // 409 Conflict
    DUPLICATE_RESOURCE(409, ExceptionCategory.BUSINESS, "이미 존재하는 리소스입니다: {0}"),

    // 500 Internal Server Error
    INTERNAL_ERROR(500, ExceptionCategory.SYSTEM, "내부 서버 오류가 발생했습니다: {0}"),
    EXTERNAL_API_ERROR(500, ExceptionCategory.SYSTEM, "외부 API 호출 중 오류가 발생했습니다: {0}"),
}
