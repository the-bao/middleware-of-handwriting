package io.github;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author rty
 * @version 1.0
 * @description:
 * @date 2025/8/30 22:09
 */
public class MyLock extends AbstractQueuedSynchronizer implements Lock {
    /*
    * state = 0 空闲
    * state = 1 被一个线程持有
    * state > 1 被重入多少次
    * */
    @Override
    protected boolean tryAcquire(int arg) {
        Thread thread = Thread.currentThread();
        int c = getState();
        if (c == 0){
            if (compareAndSetState(0,arg)){
                setExclusiveOwnerThread(thread);
                return true;
            }
        }else if (thread == getExclusiveOwnerThread()){
            // 实现可重入
            setState(c + arg);
            return true;
        }

        return false;
    }

    @Override
    protected boolean tryRelease(int arg) {
        Thread thread = Thread.currentThread();
        if (thread != getExclusiveOwnerThread())
            throw new IllegalMonitorStateException("释放锁线程不是持有锁线程");

        int c = getState() - arg;
        boolean free = false;
        if (c == 0){
            free = true;
        }
        setState(c);
        return free;
    }

    @Override
    public void lock() {
        acquire(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return tryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        release(1);
    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
