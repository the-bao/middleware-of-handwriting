package io.github;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author rty
 * @version 1.0
 * @description: 线程池实现
 * @date 2025/8/6 19:22
 */
public class MyThreadPool {
    // 核心线程是否回收
    boolean allowCoreThreadTimeOut;

    // 完成任务数 当worker thread结束时更新
    private long completedTaskCount;

    private volatile ThreadFactory threadFactory;

    // 存活时间
    int keepAliveTime;

    // 时间单位
    TimeUnit timeUnit;

    // 核心线程数
    int corePoolSize;

    // 最大线程数
    int maxSize;

    // 增加worker时加锁
    private final ReentrantLock mainLock = new ReentrantLock();

    // 线程池最大线程数 （监控使用）
    private int largestPoolSize;

    // 线程集合 （监控使用）
    private final HashSet<Worker> workers = new HashSet<>();

    /*
    * 状态控制
    * ctl 32位 低29位COUNT_BITS用于存储线程池的数量
    * 高3位用于存储线程状态
    * 111 RUNNING
    * 000 SHUTDOWN
    * 001 STOP
    * 010 TIDYING
    * 011 TERMINATED
    * */
    private final AtomicInteger ctl = new AtomicInteger(getCtl(RUNNING,0));
    private static final int COUNT_BITS = Integer.SIZE - 3;
    private static final int CAPACITY   = (1 << COUNT_BITS) - 1;

    private static final int RUNNING = -1 << COUNT_BITS;
    private static final int SHUTDOWN = 0 << COUNT_BITS;
    private static final int STOP = 1 << COUNT_BITS;
    private static final int TIDYING = 2 << COUNT_BITS;
    private static final int TERMINATED = 3 << COUNT_BITS;

    /*
    * 通过与运算得到想要的状态位 和1相与都是1，和0相与得到自己
    * 11111111111111111111111111111 CAPACITY
    * 通过或运算得到想要的ctl字段 0和任何数或都是0
    * */
    private static int getRunState(int ctl){
        return ctl & ~CAPACITY;
    }

    private static int getWorkCount(int ctl){
        return ctl & CAPACITY;
    }

    private static int getCtl(int runState, int workCount){
        return runState | workCount;
    }

    // 阻塞队列 存放任务
    private final BlockingQueue<Runnable> blockingQueue;

    // 拒绝策略
    private RejectHandler rejectHandler;

    public MyThreadPool(int corePoolSize, int maxSize, BlockingQueue<Runnable> blockingQueue,RejectHandler rejectHandler,int keepAliveTime,TimeUnit timeUnit) {
        this.corePoolSize = corePoolSize;
        this.maxSize = maxSize;
        this.blockingQueue = blockingQueue;
        this.rejectHandler = rejectHandler;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.threadFactory = Executors.defaultThreadFactory();
        this.allowCoreThreadTimeOut = false;
    }

    public MyThreadPool(int corePoolSize, int maxSize, BlockingQueue<Runnable> blockingQueue,RejectHandler rejectHandler,int keepAliveTime,TimeUnit timeUnit,ThreadFactory threadFactory) {
        this.corePoolSize = corePoolSize;
        this.maxSize = maxSize;
        this.blockingQueue = blockingQueue;
        this.rejectHandler = rejectHandler;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.threadFactory = threadFactory;
        this.allowCoreThreadTimeOut = false;
    }

    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    /*
    * 执行任务时：
    *   1.核心线程没满，则创建一个核心线程执行任务
    *   2.核心线程满了，则直接放入阻塞任务队列中等待执行
    *   3.核心线程满了，阻塞队列满了，但是辅助线程未满，则创建一个辅助线程执行任务
    *   4.核心线程满了，阻塞队列满了，辅助线程也满了，则执行拒绝策略
    * */
    void execute(Runnable task){
        // 1.核心线程没满，则创建一个核心线程执行任务
        if (getWorkCount(ctl.get()) < corePoolSize){
            if (addWorker(task,true)) return;
        }
        // 2.核心线程满了，则直接放入阻塞任务队列中等待执行
        if (blockingQueue.offer(task)){
            System.out.println(Thread.currentThread().getName() + "的任务进入阻塞队列");
            // 检查是否需要补充线程
            if (getWorkCount(ctl.get()) < corePoolSize) {
                addWorker(null,true);
            }
            return;
        }
        // 3.核心线程满了，阻塞队列满了，但是辅助线程未满，则创建一个辅助线程执行任务
        if (ctl.get() < maxSize){
            if (addWorker(task,false)){
                return;
            }
        }
        // 4.核心线程满了，阻塞队列满了，辅助线程也满了，则执行拒绝策略
        // 执行拒绝策略
        System.out.println("执行拒绝策略");
        rejectHandler.reject(task, this);
    }

    private boolean addWorker(Runnable firstTask, boolean core){
        retry:
        for (;;){
            int c = ctl.get();
            int rs = getRunState(c);
            int wc = getWorkCount(c);

            // 检查线程池的状态 所有大于等于SHUTDOWN的状态和SHUNTOWN处理新任务时都拒绝线程创建
            if (rs >= SHUTDOWN && (rs == SHUTDOWN && firstTask != null)) return false;

            for (;;){
                if (wc >= CAPACITY || wc >= (core ? corePoolSize : maxSize)) return false;
                if (ctl.compareAndSet(c,c+1)) break retry;
                c = ctl.get();
                if (getRunState(c) != rs) continue retry;
            }
        }

        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {
            w = new Worker(firstTask);
            final Thread t = w.thread;
            if (t != null){
                final ReentrantLock mainLock = this.mainLock;
                mainLock.lock();
                try {
                    int rs = getRunState(ctl.get());
                    // 仅允许RUNNING或者SHUTDOWN处理队列中剩余任务
                    if (rs == RUNNING || rs == SHUTDOWN && firstTask == null){
                        if (t.isAlive()) throw new IllegalThreadStateException();
                        workers.add(w);
                        workerAdded = true;
                    }
                }finally {
                    mainLock.unlock();
                }
                if (workerAdded){
                    t.start();
                    workerStarted = true;
                }
            }
        }finally {
            System.out.println("线程创建：" + workerStarted);
            System.out.println("当前线程数：" + workers.size());
        }
        return workerStarted;
    }

    /*
    * Worker添加Thread，用于后续新增worker的状态管理
    * 在线程池的设计中，所有的线程都是平等的，所谓的核心线程只是 boolean timed = allowCoreThreadTimeOut || wc > corePoolSize; 筛选出来的幸运儿
    * TODO 去掉核心线程队列和非核心线程队列 使用通过线程数进行管理
    * */
    class Worker implements Runnable{
        final Thread thread;
        private Runnable firstTask;
        private volatile boolean running = true;

        public Worker(Runnable firstTask){
            this.firstTask = firstTask;
            this.thread = getThreadFactory().newThread(this);
            System.out.println("Worker创建：" + thread.getName());
        }

        @Override
        public void run() {
            // firstTask 提高新任务的响应速度，无需入队，优先执行
            Runnable task = firstTask;
            firstTask = null;

            while (running){
                // 如果没有初始任务，则从队列中获取任务
                if (task == null) {
                    try {
                        // 根据是否为核心线程和是否允许超时来决定获取任务的方式 , 线程之间是平等的 , 是否核心取决于线程数量
                        boolean isCoreThread = getWorkCount(ctl.get()) <= corePoolSize;
                        if (isCoreThread && !allowCoreThreadTimeOut) {
                            // 核心线程且不允许超时，使用阻塞获取
                            task = blockingQueue.take();
                        } else {
                            // 非核心线程或允许核心线程超时，使用超时获取
                            task = blockingQueue.poll(keepAliveTime, timeUnit);
                            if (task == null) {
                                // 超时没有获取到任务，结束线程
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        running = false;
                        break;
                    }
                }

                if (task != null) {
                    try {
                        System.out.println(Thread.currentThread().getName() + ":执行一个任务");
                        task.run();
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    } finally {
                        task = null; // 清空任务引用
                    }
                }
            }
            // 从线程列表中移除
            System.out.println(Thread.currentThread().getName() + ":工作线程被回收");
        }

        public void stopWorker() {
            running = false;
            this.thread.interrupt();
        }
    }

    /**
     * A handler for rejected tasks that throws a
     * {@code RejectedExecutionException}.
     */
    public static class AbortPolicy implements RejectHandler {
        /**
         * Creates an {@code AbortPolicy}.
         */
        public AbortPolicy() { }

        @Override
        public void reject(Runnable rejectCommand, MyThreadPool theadPool) {
            System.out.println("do nothing and discard");
        }
    }

    /**
     * A handler for rejected tasks that runs the rejected task
     * directly in the calling thread of the {@code execute} method,
     * unless the executor has been shut down, in which case the task
     * is discarded.
     */
    public static class CallerRunsPolicy implements RejectHandler {
        /**
         * Creates a {@code CallerRunsPolicy}.
         */
        public CallerRunsPolicy() { }

        @Override
        public void reject(Runnable task, MyThreadPool myThreadPool) {
            task.run();
        }
    }
}
