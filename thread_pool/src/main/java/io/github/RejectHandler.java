package io.github;

public interface RejectHandler {
    void reject(Runnable task,MyThreadPool myThreadPool);
}
