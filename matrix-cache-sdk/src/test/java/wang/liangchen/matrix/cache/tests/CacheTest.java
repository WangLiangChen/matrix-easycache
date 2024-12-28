package wang.liangchen.matrix.cache.tests;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;
import wang.liangchen.matrix.cache.sdk.cache.redis.serializer.ProtostuffRedisSerializer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class CacheTest {
    @Test
    public void testTtl() {
        Cache<String, String> cache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.SECONDS)
                .build();
        for (int i = 0; i < 20; i++) {
            if (0 == i) {
                cache.put("name", "Liangchen.Wang");
            }
            if (5 == i) {
                cache.put("sex", "male");
            }
            if (10 == i) {
                cache.put("age", "42");
            }
            System.out.printf("%s,%s,%s", cache.getIfPresent("name"), cache.getIfPresent("sex"), cache.getIfPresent("age"));
            System.out.println();
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    public void testProtostuff() throws Exception {
        int count = 500;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        ProtostuffRedisSerializer<Person> serializer = new ProtostuffRedisSerializer<>();
        for (int i = 0; i < count; i++) {
            new Thread(() -> {
                String name = Thread.currentThread().getName();
                Person person = new Person();
                person.setName(name);
                byte[] bytes = serializer.serialize(person);
                try {
                    TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(10, 50));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                person = serializer.deserialize(bytes);
                System.out.println(name.equals(person.getName()));
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();

    }

    static class Person {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
