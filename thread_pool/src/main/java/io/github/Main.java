package io.github;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author rty
 * @version 1.0
 * @description: 主函数
 * @date 2025/8/6 20:26
 */
public class Main {
    public static void main(String[] args) {
        MyThreadPool myTheadPool = new MyThreadPool(2,8,new ArrayBlockingQueue(2), new MyThreadPool.AbortPolicy(),2,TimeUnit.SECONDS);

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2,8,2,TimeUnit.SECONDS,new ArrayBlockingQueue<>(2),new ThreadPoolExecutor.AbortPolicy());

        for (int i = 0; i < 10; i++) {
            final int fi = i;
            myTheadPool.execute(() -> {
                try {
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    throw new RuntimeException(e);
                }
                System.out.println(Thread.currentThread().getName() + " " + fi);
            });
        }

        System.out.println("主线程没有被阻塞");
    }
}
