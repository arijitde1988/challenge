
package com.dws.challenge.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;

import lombok.extern.apachecommons.CommonsLog;


/**
 * Lock Util to acquire lock on top of AccountNo
 * lock used ReentrantLock java.util.concurrent.locks.ReentrantLock.ReentrantLock()
 * @author Arijit De
 * */
@Component
public class LockUtilByAccountNo {
	
	private static Map<String, LockWrapper> locks = new ConcurrentHashMap<String, LockWrapper>();
    
    private static class LockWrapper {
        private final Lock lock = new ReentrantLock();
        private final AtomicInteger numberOfThreadsInQueue = new AtomicInteger(1);
        
        private LockWrapper addThreadInQueue() {
            numberOfThreadsInQueue.incrementAndGet(); 
            return this;
        }
        
        private int removeThreadFromQueue() {
            return numberOfThreadsInQueue.decrementAndGet(); 
        }
        
    }
    
    /**
     * Used to acquire lock on top of accountNo
     * @param accountNo Account No no top of which lock acquired
     * @author Arijit De
     * */
    public void lock(String accountNo) {
        LockWrapper lockWrapper = locks.compute(accountNo, (k, v) -> v == null ? new LockWrapper() : v.addThreadInQueue());
        lockWrapper.lock.lock();
    }
    
    /**
     * Used to release lock from accountNo
     * @param accountNo Account No no top of which lock acquired
     * @author Arijit De
     * */
    public void unlock(String accountNo) {
        LockWrapper lockWrapper = locks.get(accountNo);
        lockWrapper.lock.unlock();
        if (lockWrapper.removeThreadFromQueue() == 0) { 
            locks.remove(accountNo, lockWrapper);
        }
    }
    
}