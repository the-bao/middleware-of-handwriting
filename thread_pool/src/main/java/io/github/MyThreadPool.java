package io.github;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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

    // 核心线程队列
    List<Worker> coreThreadList;

    // 辅助线程队列
    List<Worker> supportThreadList;

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
        coreThreadList = new CopyOnWriteArrayList<>();
        supportThreadList = new CopyOnWriteArrayList<>();
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
        coreThreadList = new CopyOnWriteArrayList<>();
        supportThreadList = new CopyOnWriteArrayList<>();
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
        if (coreThreadList.size() < corePoolSize){
            Worker worker = new Worker(task);
            coreThreadList.add(worker);
            final Thread t = worker.thread;
            t.start();
            System.out.println(t.getName() + ":核心工作线程创建");
            return;
        }
        // 2.核心线程满了，则直接放入阻塞任务队列中等待执行
        if (blockingQueue.offer(task)){
            System.out.println(Thread.currentThread().getName() + "的任务进入阻塞队列");
            // 检查是否需要补充线程
            if (coreThreadList.isEmpty() && coreThreadList.size() < corePoolSize) {
                Worker worker = new Worker(null);
                coreThreadList.add(worker);
                final Thread t = worker.thread;
                t.start();
                System.out.println(t.getName() + ":核心工作线程创建");
            }
            return;
        }
        // 3.核心线程满了，阻塞队列满了，但是辅助线程未满，则创建一个辅助线程执行任务
        if (coreThreadList.size() + supportThreadList.size() < maxSize){
            Worker worker = new Worker(task);
            supportThreadList.add(worker);
            final Thread t = worker.thread;
            t.start();
            System.out.println(t.getName() + ":辅助工作线程创建");
            return;
        }
        // 4.核心线程满了，阻塞队列满了，辅助线程也满了，则执行拒绝策略
        // 执行拒绝策略
        System.out.println("执行拒绝策略");
        rejectHandler.reject(task, this);
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
                        // 根据是否为核心线程和是否允许超时来决定获取任务的方式
                        boolean isCoreThread = coreThreadList.contains(this);
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
            coreThreadList.remove(this);
            supportThreadList.remove(this);
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
