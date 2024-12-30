package wang.liangchen.matrix.cache.sdk.cache.caffeine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wang.liangchen.matrix.cache.sdk.cache.MatrixCache;

import java.time.Duration;
import java.util.Set;

/**
 * @author LiangChen.Wang
 */
public class MatrixCaffeineMatrixCache extends org.springframework.cache.caffeine.CaffeineCache implements MatrixCache {
    private static final Logger logger = LoggerFactory.getLogger(MatrixCaffeineMatrixCache.class);
    /**
     * time to live - ttl
     * time to idle - tti
     */
    private final Duration ttl;

    public MatrixCaffeineMatrixCache(String name, com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache, boolean allowNullValues, Duration ttl, MatrixCaffeineRemovalListener removalListener) {
        super(name, nativeCache, allowNullValues);
        this.ttl = ttl;
        removalListener.registerDelegate((key, value, cause) -> {
            logger.debug("Cache '{}' remove key '{}' due to '{}'", name, key, cause);
        });
    }


    @Override
    public Set<Object> keys() {
        return this.getNativeCache().asMap().keySet();
    }

    @Override
    public boolean containsKey(Object key) {
        return keys().contains(key);
    }

    @Override
    public Duration getTtl() {
        return this.ttl;
    }

    @Override
    public String toString() {
        return "MatrixCaffeineMatrixCache{" +
                "name='" + getName() + '\'' +
                ", ttl=" + ttl +
                ", allowNullValues=" + isAllowNullValues() +
                '}';
    }
}
