package com.demo.common.cache.local

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

@Configuration
class CacheMetricsConfig {
    @Bean
    fun caffeineCacheMetrics(cacheManager: CacheManager): MeterBinder {
        return MeterBinder { registry: MeterRegistry? ->
            cacheManager.cacheNames.forEach(
                Consumer { cacheName: String? ->
                    val cache = cacheManager.getCache(cacheName!!)
                    if (cache is CaffeineCache) {
                        val nativeCache = cache.nativeCache

                        // 히트율 메트릭
                        Gauge.builder("cache.hit.ratio") { nativeCache.stats().hitRate() }
                            .tag("cache", cacheName)
                            .register(registry!!)

                        // 캐시 크기 메트릭
                        Gauge.builder("cache.size") { nativeCache.estimatedSize() }
                            .tag("cache", cacheName)
                            .register(registry)
                    }
                },
            )
        }
    }
}
