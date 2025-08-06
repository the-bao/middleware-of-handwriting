package io.github;

import java.util.List;

/**
 * @author rty
 * @version 1.0
 * @description: 不丢弃的线程池拒绝策略
 * @date 2025/8/6 20:56
 */
public class AcceptPolicy implements RejectHandler{

    @Override
    public void reject(Runnable task, MyThreadPool myThreadPool) {

    }
}
