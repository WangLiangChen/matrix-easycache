package wang.liangchen.matrix.cache.sdk.cache.redis;


import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import wang.liangchen.matrix.cache.sdk.cache.AbstractMatrixCacheManager;

import java.time.Duration;

/**
 * @author LiangChen.Wang 2021/4/16
 */
public class MatrixRedisMatrixCacheManager extends AbstractMatrixCacheManager {
    private final RedisCacheConfiguration cacheConfig;
    private final StringRedisTemplate redisTemplate;
    private final RedisCacheConfiguration defaultCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();

    public MatrixRedisMatrixCacheManager(RedisCacheConfiguration cacheConfig, StringRedisTemplate redisTemplate) {
        this.cacheConfig = cacheConfig;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected Cache getMissingCache(String name, Duration ttl) {
        RedisCacheConfiguration localConfig = cacheConfig == null ? defaultCacheConfiguration : cacheConfig;
        if (Duration.ZERO.compareTo(ttl) < 0) {
            localConfig = localConfig.entryTtl(ttl);
        }
        if (!isAllowNullValues()) {
            localConfig = localConfig.disableCachingNullValues();
        }
        return new MatrixRedisMatrixCache(name, localConfig, redisTemplate);
    }

    public StringRedisTemplate getRedisTemplate() {
        return redisTemplate;
    }
}
