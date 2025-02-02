package com.demo.common.cache.fallback

import org.slf4j.LoggerFactory
import org.springframework.cache.Cache
import org.springframework.dao.QueryTimeoutException
import java.util.concurrent.Callable

class FallbackCache(
    private val primaryCache: Cache,
    private val secondaryCache: Cache?,
) : Cache {
    override fun getName(): String = primaryCache.name

    override fun getNativeCache(): Any = primaryCache.nativeCache

    override fun get(key: Any): Cache.ValueWrapper? {
        try {
            log.info("Redis에서 키 [{}] 조회 시도", key)

            primaryCache.get(key)?.let {
                log.info("Redis에서 키 [{}] 조회 성공", key)
                return it
            }
        } catch (e: Exception) {
            when (e) {
                is QueryTimeoutException -> {
                    log.error("Redis 타임아웃 발생. 키 [{}]. 로컬 캐시에서 조회", key)
                }

                else -> {
                    log.error("Redis 조회 실패. 키 [{}]: {}. 로컬 캐시에서 조회", key, e.message)
                }
            }

            return secondaryCache?.get(key).also { value ->
                if (value != null) {
                    log.info("로컬 캐시에서 키 [{}] 조회 성공", key)
                } else {
                    log.info("로컬 캐시에도 키 [{}] 없음", key)
                }
            }
        }

        return secondaryCache?.get(key).also { value ->
            if (value != null) {
                log.info("로컬 캐시에서 키 [{}] 조회 성공", key)
            }
        }
    }

    override fun <T> get(
        key: Any,
        type: Class<T>?,
    ): T? {
        try {
            log.info("Redis에서 키 [{}], 타입 [{}] 조회 시도", key, type?.simpleName)

            primaryCache.get(key, type)?.let {
                log.info("Redis에서 키 [{}], 타입 [{}] 조회 성공", key, type?.simpleName)
                return it
            }
        } catch (e: Exception) {
            when (e) {
                is QueryTimeoutException -> {
                    log.error("Redis 타임아웃 발생. 키 [{}], 타입 [{}]. 로컬 캐시에서 조회", key, type?.simpleName)
                }

                else -> {
                    log.error(
                        "Redis 조회 실패. 키 [{}], 타입 [{}]: {}. 로컬 캐시에서 조회",
                        key,
                        type?.simpleName,
                        e.message,
                    )
                }
            }

            return secondaryCache?.get(key, type).also { value ->
                if (value != null) {
                    log.info("로컬 캐시에서 키 [{}], 타입 [{}] 조회 성공", key, type?.simpleName)
                } else {
                    log.info("로컬 캐시에도 키 [{}], 타입 [{}] 없음", key, type?.simpleName)
                }
            }
        }

        return secondaryCache?.get(key, type).also { value ->
            if (value != null) {
                log.info("로컬 캐시에서 키 [{}], 타입 [{}] 조회 성공", key, type?.simpleName)
            }
        }
    }

    override fun <T : Any> get(
        key: Any,
        valueLoader: Callable<T>,
    ): T? {
        try {
            log.info("Redis에서 키 [{}] 조회 시도", key)
            return primaryCache.get(key, valueLoader)
        } catch (e: Exception) {
            when (e) {
                is QueryTimeoutException -> {
                    log.error("Redis 타임아웃 발생. 키 [{}]. 로컬 캐시로 전환", key)
                }

                else -> {
                    log.error("Redis 조회 실패. 키 [{}]: {}. 로컬 캐시로 전환", key, e.message)
                }
            }

            // Redis 실패 시 Caffeine에서 조회
            secondaryCache?.get(key)?.get()?.let {
                log.info("로컬 캐시에서 키 [{}] 조회 성공", key)
                return it as T
            }

            // Caffeine에도 없는 경우 valueLoader 실행
            return try {
                log.info("로컬 캐시에도 키 [{}]가 없어 새로운 값을 생성", key)
                valueLoader.call().also { value ->
                    // 새로 로드된 값을 양쪽 캐시에 저장
                    secondaryCache?.put(key, value)
                    log.info("새로운 값을 로컬 캐시에 저장. 키: [{}]", key)

                    try {
                        primaryCache.put(key, value)
                        log.info("새로운 값을 Redis에도 저장. 키: [{}]", key)
                    } catch (e: Exception) {
                        when (e) {
                            is QueryTimeoutException -> {
                                log.error("Redis 저장 타임아웃. 키 [{}]. 로컬 캐시만 사용", key)
                            }

                            else -> {
                                log.error("Redis 저장 실패. 키 [{}]: {}. 로컬 캐시만 사용", key, e.message)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                log.error("값 생성 실패. 키 [{}]: {}", key, e.message)
                throw e
            }
        }
    }

    override fun put(
        key: Any,
        value: Any?,
    ) {
        try {
            primaryCache.put(key, value)
            log.info("Redis에 키 [{}] 저장 성공", key)
        } catch (e: Exception) {
            when (e) {
                is QueryTimeoutException -> {
                    log.error("Redis 저장 타임아웃. 키 [{}]. 로컬 캐시만 사용", key)
                }

                else -> {
                    log.error("Redis 저장 실패. 키 [{}]: {}. 로컬 캐시만 사용", key, e.message)
                }
            }
        }
        secondaryCache?.put(key, value)
        log.info("로컬 캐시에 키 [{}] 저장 완료", key)
    }

    override fun evict(key: Any) {
        try {
            primaryCache.evict(key)
            log.info("Redis에서 키 [{}] 삭제 성공", key)
        } catch (e: Exception) {
            when (e) {
                is QueryTimeoutException -> {
                    log.error("Redis 삭제 타임아웃. 키 [{}]", key)
                }

                else -> {
                    log.error("Redis 삭제 실패. 키 [{}]: {}", key, e.message)
                }
            }
        }
        secondaryCache?.evict(key)
        log.info("로컬 캐시에서 키 [{}] 삭제 완료", key)
    }

    override fun clear() {
        try {
            primaryCache.clear()
            log.info("Redis 초기화 성공")
        } catch (e: Exception) {
            when (e) {
                is QueryTimeoutException -> {
                    log.error("Redis 초기화 타임아웃")
                }

                else -> {
                    log.error("Redis 초기화 실패: {}", e.message)
                }
            }
        }
        secondaryCache?.clear()
        log.info("로컬 캐시 초기화 완료")
    }

    companion object {
        private val log = LoggerFactory.getLogger(FallbackCache::class.java)
    }
}
