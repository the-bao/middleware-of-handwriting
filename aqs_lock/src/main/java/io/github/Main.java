package io.github;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author rty
 * @version 1.0
 * @description:
 * @date 2025/8/30 16:47
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        final int[] count = {0};
        //ReentrantLock lock = new ReentrantLock();
        MyLock lock = new MyLock();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                lock.lock();
                try {
                    lock.lock();
                    count[0]++;
                    lock.unlock();
                }finally {
                    lock.unlock();
                }
            });
            threads.add(thread);
            thread.start();
        }

        // 等待所有线程执行完毕
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println(count[0]);
    }
}
