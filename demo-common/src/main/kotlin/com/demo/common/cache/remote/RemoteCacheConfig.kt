package com.demo.common.cache.remote

import com.fasterxml.jackson.databind.ObjectMapper
import io.lettuce.core.ReadFrom
import io.lettuce.core.TimeoutOptions
import io.lettuce.core.cluster.ClusterClientOptions
import io.lettuce.core.resource.ClientResources
import io.lettuce.core.resource.DefaultClientResources
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
class RemoteCacheConfig(
    @Value("\${spring.data.redis.host}") private val redisHost: String,
    @Value("\${spring.data.redis.port}") private val redisPort: Int,
    @Value("\${spring.data.redis.timeout}") private val timeout: Long,
    @Value("\${spring.data.redis.retry.max-attempts}") private val maxAttempts: Int,
    @Value("\${spring.data.redis.pool.netty-threads}") private val nettyThreads: Int,
) {
    @Bean
    fun remoteCacheManager(
        redisConnectionFactory: RedisConnectionFactory,
        objectMapper: ObjectMapper,
    ): RedisCacheManager {
        val configurations =
            CACHE_CONFIGS.mapValues { (_, config) ->
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(config.ttl) // 캐시 이름에 맞는 TTL 설정
                    .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()))
                    .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                            GenericJackson2JsonRedisSerializer(),
                        ),
                    )
                    .disableCachingNullValues()
            }

        return RedisCacheManager.builder(redisConnectionFactory)
            .withInitialCacheConfigurations(configurations) // 캐시 이름별 TTL 설정 반영
            .build()
    }

    @Bean
    fun lettuceConnectionFactory(clientResources: ClientResources): LettuceConnectionFactory {
        val clientOptions =
            ClusterClientOptions.builder()
                .timeoutOptions(TimeoutOptions.enabled(Duration.ofMillis(timeout)))
                .autoReconnect(true)
                .maxRedirects(maxAttempts)
                .validateClusterNodeMembership(false) // 클러스터 노드 검증 비활성화
                .build()

        val clientConfig =
            LettuceClientConfiguration.builder()
                .clientResources(clientResources)
                .clientOptions(clientOptions)
                .commandTimeout(Duration.ofMillis(timeout))
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .build()

        val redisConfig = RedisStandaloneConfiguration(redisHost, redisPort)

        return LettuceConnectionFactory(redisConfig, clientConfig).apply {
            eagerInitialization = true
        }
    }

    @Bean
    fun <T> redisTemplate(
        redisConnectionFactory: RedisConnectionFactory,
        objectMapper: ObjectMapper,
    ): RedisTemplate<String, T> {
        val redisTemplate = RedisTemplate<String, T>()
        redisTemplate.connectionFactory = redisConnectionFactory
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
        return redisTemplate
    }

    @Bean
    fun clientResources(): ClientResources {
        return DefaultClientResources.builder()
            .ioThreadPoolSize(nettyThreads)
            .computationThreadPoolSize(nettyThreads)
            .build()
    }

    companion object {
        private val CACHE_CONFIGS =
            mapOf(
                "staticDataCache" to
                    CacheConfigData(
                        ttl = Duration.ofHours(24),
                        description = "정적 데이터용 캐시 (코드, 카테고리 등)",
                    ),
                "frequentDataCache" to
                    CacheConfigData(
                        ttl = Duration.ofMinutes(30),
                        description = "자주 조회되는 데이터용 캐시 (상품, 게시글 등)",
                    ),
                "realtimeDataCache" to
                    CacheConfigData(
                        ttl = Duration.ofSeconds(30),
                        description = "실시간성 데이터용 캐시 (재고, 좌석 등)",
                    ),
                "largeDataCache" to
                    CacheConfigData(
                        ttl = Duration.ofMinutes(10),
                        description = "대용량 데이터용 캐시 (검색 결과 등)",
                    ),
            )
    }
}

private data class CacheConfigData(
    val ttl: Duration,
    val description: String,
)
