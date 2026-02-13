package com.booktalk_be.springconfig;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 관리자 페이지 캐시 매니저
     *
     * 캐시 대상:
     * - boardAdminList: 관리자 게시글 목록/검색 (TTL 30초)
     * - replyAdminList: 관리자 댓글 목록/검색 (TTL 30초)
     *
     * 설계 근거:
     * - COUNT 쿼리가 전체 응답 시간의 90% 이상 차지 (board 100만행 ~1,000ms, reply 250만행 ~2,200ms)
     * - 관리자 페이지 특성상 동일 페이지 재방문 빈도 높음
     * - TTL 30초로 제재/복구 반영과 캐시 효율 간 균형
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .maximumSize(200)
        );
        return cacheManager;
    }
}
