package com.ds.devsuaccount.infraestructure.cache;

import com.ds.devsuaccount.infraestructure.valuestorage.entity.Idempotency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class CacheService {
    @Autowired
    private CacheManager cacheManager;

    public void saveIdempotencyCache(String key, Idempotency value) {
        Cache cache = cacheManager.getCache("idempotencyCache");
        if (cache != null) {
            cache.put(key, value);
        }
    }

    public Idempotency getFromIdempotencyCache(String key) {
        Cache cache = cacheManager.getCache("idempotencyCache");
        if (cache != null) {
            return cache.get(key, Idempotency.class);
        }
        return null;
    }
}
