package com.demo.common.cache.fallback

import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.cache.RedisCacheManager

@Configuration
class FallbackCacheConfig {
    @Bean
    @Primary
    fun layeredCacheManager(
        redisCacheManager: RedisCacheManager,
        localCacheManager: CacheManager,
    ): CacheManager {
        return FallbackCacheManager(redisCacheManager, localCacheManager)
    }
}
