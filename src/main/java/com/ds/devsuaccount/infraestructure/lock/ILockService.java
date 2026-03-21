package com.ds.devsuaccount.infraestructure.lock;

public interface ILockService {
    boolean acquireLock(String key);

    void releaseLock(String key);

    void isLocked(String key);
}
