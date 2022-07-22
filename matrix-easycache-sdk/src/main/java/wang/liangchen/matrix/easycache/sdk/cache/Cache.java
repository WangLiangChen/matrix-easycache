package wang.liangchen.matrix.easycache.sdk.cache;

import java.time.Duration;
import java.util.Set;

/**
 * @author LiangChen.Wang 2021/3/22
 */
public interface Cache extends org.springframework.cache.Cache {

    default void evictLocal(Object key) {

    }

    default void clearLocal() {

    }

    Set<Object> keys();

    boolean containsKey(Object key);

    Duration getTtl();
}
