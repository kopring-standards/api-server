package com.demo.common.cache.fallback

import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.redis.cache.RedisCacheManager

@EnableCaching
class FallbackCacheManager(
    private val remoteCacheManager: RedisCacheManager,
    private val localCacheManager: CacheManager,
) : CacheManager {
    override fun getCache(name: String): Cache? {
        val remoteCache = remoteCacheManager.getCache(name)
        val localCache = localCacheManager.getCache(name)

        if (remoteCache == null) {
            return localCache
        }

        return FallbackCache(remoteCache, localCache)
    }

    override fun getCacheNames(): Collection<String> {
        return LinkedHashSet<String>().apply {
            addAll(remoteCacheManager.cacheNames)
            addAll(localCacheManager.cacheNames)
        }
    }
}
