package com.demo.common.cache.local

import com.github.benmanes.caffeine.cache.Caffeine
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.function.Consumer

@Configuration
@EnableCaching
class CacheConfig {
    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = SimpleCacheManager()
        val caches: MutableList<CaffeineCache> = ArrayList()

        // 각 용도별 캐시를 생성하고 리스트에 추가
        caches.add(buildStaticDataCache()) // 정적 데이터용 (코드, 카테고리 등)
        caches.add(buildFrequentDataCache()) // 자주 조회되는 데이터용 (상품, 게시글 등)
        caches.add(buildRealtimeDataCache()) // 실시간성 데이터용 (재고, 좌석 등)
        caches.add(buildLargeDataCache()) // 대용량 데이터용 (검색 결과 등)

        cacheManager.setCaches(caches)
        return cacheManager
    }

    /**
     * 정적 데이터용 캐시 구성
     */
    private fun buildStaticDataCache(): CaffeineCache {
        return CaffeineCache(
            "staticDataCache",
            Caffeine.newBuilder()
                .initialCapacity(100) // 캐시 초기 키-값 항목(엔트리)의 개수
                .maximumSize(1000) // 캐시 최대 키-값 항목(엔트리)의 개수
                .expireAfterWrite(Duration.ofHours(24)) // 마지막 쓰기 후 24시간 뒤 만료
                .recordStats() // 캐시 사용 통계 수집 활성화
                .build(),
        )
    }

    /**
     * 자주 조회되는 데이터용 캐시 구성
     */
    private fun buildFrequentDataCache(): CaffeineCache {
        return CaffeineCache(
            "frequentDataCache",
            Caffeine.newBuilder()
                .initialCapacity(500) // 캐시 초기 키-값 항목(엔트리)의 개수
                .maximumSize(5000) // 캐시 최대 키-값 항목(엔트리)의 개수
                .expireAfterWrite(Duration.ofMinutes(30)) // 데이터 작성/수정 후 30분 뒤 만료
                .expireAfterAccess(Duration.ofMinutes(15)) // 마지막 접근 후 15분 동안 미사용시 만료
                .recordStats()
                .build(),
        )
    }

    /**
     * 실시간성 데이터용 캐시 구성
     */
    private fun buildRealtimeDataCache(): CaffeineCache {
        return CaffeineCache(
            "realtimeDataCache",
            Caffeine.newBuilder()
                .initialCapacity(100) // 캐시 초기 키-값 항목(엔트리)의 개수
                .maximumSize(1000) // 캐시 최대 키-값 항목(엔트리)의 개수
                .expireAfterWrite(Duration.ofSeconds(30)) // 30초 후 만료 (실시간성 보장)
                .weakValues() // 메모리 압박시 GC 대상이 되도록 약한 참조 사용
                .recordStats()
                .build(),
        )
    }

    /**
     * 대용량 데이터용 캐시 구성
     */
    private fun buildLargeDataCache(): CaffeineCache {
        return CaffeineCache(
            "largeDataCache",
            Caffeine.newBuilder()
                .initialCapacity(200) // 캐시 초기 키-값 항목(엔트리)의 개수
                .maximumSize(2000) // 캐시 최대 키-값 항목(엔트리)의 개수
                .expireAfterWrite(Duration.ofMinutes(10)) // 10분 후 만료
                .softValues() // 메모리 부족 상황에서만 GC 대상이 되는 소프트 참조 사용
                .recordStats()
                .build(),
        )
    }

    /**
     * 캐시 모니터링을 위한 메트릭 수집
     */
    @Component
    class CacheMonitor(private val cacheManager: CacheManager) {
        @Scheduled(fixedRate = 60000) // 1분마다 실행
        fun monitorCache() {
            val cacheNames = cacheManager.cacheNames

            cacheNames.forEach(
                Consumer { cacheName: String? ->
                    val cache = cacheManager.getCache(cacheName!!)
                    if (cache is CaffeineCache) {
                        val nativeCache = cache.nativeCache

                        val stats = nativeCache.stats()

                        log.info(
                            """
                            Cache '{}' Statistics:
                            ================================
                            Hit Count: {}
                            Miss Count: {}
                            Hit Rate: {}%
                            Eviction Count: {}
                            ================================
                            
                            """.trimIndent(),
                            cacheName,
                            stats.hitCount(),
                            stats.missCount(),
                            String.format("%.2f", stats.hitRate() * 100),
                            stats.evictionCount(),
                        )
                    }
                },
            )
        }
    }

    // 스케줄러 활성화를 위한 설정
    @Configuration
    @EnableScheduling
    inner class SchedulingConfig

    private companion object {
        private val log = LoggerFactory.getLogger(CacheConfig::class.java)
    }
}
