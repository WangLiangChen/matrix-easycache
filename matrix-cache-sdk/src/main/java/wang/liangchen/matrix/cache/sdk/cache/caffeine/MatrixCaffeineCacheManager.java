package wang.liangchen.matrix.cache.sdk.cache.caffeine;


import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import wang.liangchen.matrix.cache.sdk.cache.AbstractCacheManager;
import wang.liangchen.matrix.cache.sdk.cache.Cache;

import java.time.Duration;

/**
 * @author LiangChen.Wang 2021/4/15
 */
public class MatrixCaffeineCacheManager extends AbstractCacheManager {
    private CacheLoader<Object, Object> cacheLoader;
    private CaffeineSpec caffeineSpec;

    public void setCacheLoader(CacheLoader<Object, Object> cacheLoader) {
        this.cacheLoader = cacheLoader;
    }

    public void setCacheSpecification(String specification) {
        this.caffeineSpec = CaffeineSpec.parse(specification);
    }

    @Override
    protected Cache getMissingCache(String name, Duration ttl) {
        return caffeineCache(name, ttl);
    }

    private MatrixCaffeineCache caffeineCache(String name, Duration ttl) {
        return new MatrixCaffeineCache(name, nativeCache(ttl), isAllowNullValues(), ttl);
    }

    private com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache(Duration ttl) {
        Caffeine<Object, Object> cacheBuilder = null == this.caffeineSpec ? Caffeine.newBuilder() : Caffeine.from(caffeineSpec);
        if (Duration.ZERO.compareTo(ttl) < 0) {
            cacheBuilder.expireAfterWrite(ttl);
        }
        return (this.cacheLoader != null ? cacheBuilder.build(this.cacheLoader) : cacheBuilder.build());
    }

}