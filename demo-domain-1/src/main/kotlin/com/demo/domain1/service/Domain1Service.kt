package com.demo.domain1.service

import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Domain1Service {
    /**
     * 정적 데이터 캐시 사용 예시 (카테고리 정보)
     */
    @Cacheable(cacheNames = ["staticDataCache"], key = "'category:' + #categoryId")
    fun getCategory(categoryId: Long) {
    }

    /**
     * 자주 조회되는 데이터 캐시 사용 예시 (상품 기본 정보)
     */
    @Cacheable(
        cacheNames = ["frequentDataCache"],
        key = "'product:' + #productId",
        unless = "#result == null",
    )
    fun getProduct(productId: Long) {
    }

    /**
     * 실시간성 데이터 캐시 사용 예시 (재고 정보)
     */
    @Cacheable(
        cacheNames = ["realtimeDataCache"],
        key = "'stock:' + #productId",
    )
    fun getProductStock(productId: Long) {
    }

    /**
     * 대용량 데이터 캐시 사용 예시 (검색 결과)
     */
    @Cacheable(
        cacheNames = ["largeDataCache"],
        key = "'search:' + #keyword + ':page:' + #page",
    )
    fun searchProducts(
        keyword: String,
        page: Int,
    ) {
    }

    /**
     * 캐시 업데이트 예시
     */
    @CachePut(
        cacheNames = ["frequentDataCache"],
        key = "'product:' + #result.id",
    )
    fun updateProduct() {
    }

    /**
     * 캐시 삭제 예시
     */
    @CacheEvict(
        cacheNames = ["frequentDataCache", "realtimeDataCache"],
        key = "'product:' + #productId",
    )
    fun deleteProduct(productId: Long) {
    }

    /**
     * 여러 캐시 동시 사용 예시
     */
    @Caching(
        cacheable = [
            Cacheable(cacheNames = ["frequentDataCache"], key = "'product:' + #productId"),
            Cacheable(cacheNames = ["realtimeDataCache"], key = "'stock:' + #productId"),
        ],
    )
    fun getProductWithStock(productId: Long) {
    }
}
