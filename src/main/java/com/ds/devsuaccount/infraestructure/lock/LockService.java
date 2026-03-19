package com.ds.devsuaccount.infraestructure.lock;

import com.ds.devsuaccount.infraestructure.exceptions.ApiException;
import com.ds.devsuaccount.infraestructure.exceptions.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Slf4j
@Profile("!local")
public class LockService implements ILockService {

    private final Jedis jedis;
    private final int ttlSeconds;

    private final ConcurrentMap<String, String> localLocks = new ConcurrentHashMap<>();

    public LockService(
            @Value("${redis.host}") String redisHost,
            @Value("${redis.port}") int redisPort,
            @Value("${lock.ttl-millis}") long ttlMillis) {
        this.jedis = new Jedis(redisHost, redisPort);
        this.ttlSeconds = (int) (ttlMillis / 1000);
    }

    @Override
    public boolean acquireLock(String key) {
        try {
            isLocked(key);

            String uuid = UUID.randomUUID().toString();
            SetParams params = new SetParams().nx().ex(ttlSeconds);
            String result = jedis.set(key, uuid, params);

            boolean acquired = "OK".equals(result);
            if (acquired) {
                localLocks.put(key, uuid);
            }
            return acquired;
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error locking resource id:{}", key, ex);
            throw new ApiException(ErrorCode.ERROR_LOCKING_RESOURCE);
        }
    }

    @Override
    public void releaseLock(String key) {
        String uuid = localLocks.get(key);
        String luaScript =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "   return redis.call('del', KEYS[1]) " +
                        "else " +
                        "   return 0 " +
                        "end";
        Object result = jedis.eval(luaScript, 1, key, uuid);

        boolean released = Long.valueOf(1L).equals(result);
        if (released) {
            localLocks.remove(key);
        }
    }

    public void isLocked(String key) {
        if (localLocks.containsKey(key) || jedis.exists(key)) {
            log.warn("Resources already locked.");
            throw new ApiException(ErrorCode.RESOURCE_ALREADY_LOCKED);
        }
    }
}
