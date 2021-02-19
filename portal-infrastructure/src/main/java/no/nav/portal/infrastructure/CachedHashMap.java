package no.nav.portal.infrastructure;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class CachedHashMap<K, V> {
    private final Duration timeToLive;

    public V getOrCompute(K key, Supplier<Optional<V>> supplier) {
        Entry<V> result = cache.get(key);
        if (result != null && result.isExpired()) {
            return result.value;
        }
        Optional<V> value = supplier.get();
        if (value.isEmpty()) {
            return null;
        }
        cache.put(key, new Entry<>(ZonedDateTime.now().plus(timeToLive), value.get()));
        return value.get();
    }

    private static class Entry<V> {
        private final ZonedDateTime expiry;
        private final V value;

        private Entry(ZonedDateTime expiry, V value) {
            this.expiry = expiry;
            this.value = value;
        }

        public boolean isExpired() {
            return ZonedDateTime.now().isAfter(expiry);
        }
    }

    private Map<K, Entry<V>> cache = new HashMap<>();

    public CachedHashMap(Duration timeToLive) {
        this.timeToLive = timeToLive;
    }
}
