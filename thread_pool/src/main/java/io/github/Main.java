package io.github;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) {
        MyThreadPool myTheadPool = new MyThreadPool(2,8,new ArrayBlockingQueue(2), new MyThreadPool.AbortPolicy(),1,TimeUnit.SECONDS);

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2,8,2,TimeUnit.SECONDS,new ArrayBlockingQueue<>(2),new ThreadPoolExecutor.AbortPolicy());

        for (int i = 0; i < 10; i++) {
            final int fi = i;
            myTheadPool.execute(() -> {
                try {
                    Thread.sleep(5*1000);
                }catch (InterruptedException e){
                    throw new RuntimeException(e);
                }

                logger.info("++++++++++++++++++++++++++++++++++++++++");
                logger.info(Thread.currentThread().getName() + " " + fi);
                logger.info("目前已完成的任务数：" + myTheadPool.getCompletedTaskCount());
                logger.info("目前活跃线程数：" + myTheadPool.getActiveCount());
                logger.info("总任务数：" + myTheadPool.getTaskCount());
                logger.info("++++++++++++++++++++++++++++++++++++++++");
            });
        }

        System.out.println("主线程没有被阻塞");
    }
}
