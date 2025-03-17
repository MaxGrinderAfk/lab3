package idespring.lab3.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CacheConfig<K, V> {
    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final long maxAgeInMillis;
    private final int maxSize;

    public CacheConfig(@Value("${cache.maxAge}") long maxAgeInMillis,
                       @Value("${cache.maxSize}") int maxSize) {
        this.maxAgeInMillis = maxAgeInMillis;
        this.maxSize = maxSize;
    }

    public void put(K key, V value) {
        if (cache.size() >= maxSize) {
            return;
        }

        cache.put(key, value);

        executor.schedule(() -> remove(key), maxAgeInMillis, TimeUnit.MILLISECONDS);
    }

    public V get(K key) {
        return cache.get(key);
    }

    public void remove(K key) {
        cache.remove(key);
    }

    public int size() {
        return cache.size();
    }

    public void shutdown() {
        executor.shutdown();
    }
}
