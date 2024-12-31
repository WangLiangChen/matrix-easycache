package wang.liangchen.matrix.cache.sdk.cache.redis;

import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import wang.liangchen.matrix.cache.sdk.cache.MatrixCache;
import wang.liangchen.matrix.cache.sdk.consistency.RedisSynchronizer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * 扩展补充spring的RedisCache
 *
 * @author LiangChen.Wang
 */
public class MatrixRedisMatrixCache extends org.springframework.data.redis.cache.RedisCache implements MatrixCache {
    private final Duration ttl;
    private final StringRedisTemplate redisTemplate;
    private final ScanOptions scanOptions;

    public MatrixRedisMatrixCache(String name, RedisCacheConfiguration redisCacheConfiguration, StringRedisTemplate redisTemplate) {
        super(name, RedisCacheWriter.nonLockingRedisCacheWriter(redisTemplate.getRequiredConnectionFactory(), BatchStrategies.scan(512)), redisCacheConfiguration);
        this.redisTemplate = redisTemplate;
        this.ttl = redisCacheConfiguration.getTtlFunction().getTimeToLive(Object.class, null);
        scanOptions = ScanOptions.scanOptions().match(this.createCacheKey("*")).count(512).build();
    }

    @Override
    public void evict(Object key) {
        super.evict(key);
        // add to evict queue
        RedisSynchronizer.INSTANCE.sendEvictMessage(this.getName(), String.valueOf(key));
    }

    @Override
    public void clear() {
        super.clear();
        RedisSynchronizer.INSTANCE.sendEvictMessage(this.getName());
    }

    @Override
    public Set<Object> keys() {
        return redisTemplate.execute((RedisCallback<Set<Object>>) redisConnection -> {
            Set<Object> keySet = new HashSet<>();
            if (redisConnection instanceof RedisClusterConnection clusterNodeConnection) {
                clusterNodeConnection.clusterGetNodes().forEach(clusterNode -> {
                    if (clusterNode.isMaster()) {
                        populateKeys(clusterNodeConnection, keySet);
                    }
                });
                return keySet;
            }
            populateKeys(redisConnection, keySet);
            return keySet;
        });
    }

    private void populateKeys(RedisConnection redisConnection, Set<Object> keySet) {
        try (Cursor<byte[]> cursor = redisConnection.keyCommands().scan(scanOptions)) {
            while (cursor.hasNext()) {
                keySet.add(new String(cursor.next(), StandardCharsets.UTF_8));
            }
        }
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
        return "MatrixRedisMatrixCache{" +
                "name=" + this.getName() +
                ", ttl=" + ttl +
                ", scanOptions=" + scanOptions.toOptionString() +
                '}';
    }
}
