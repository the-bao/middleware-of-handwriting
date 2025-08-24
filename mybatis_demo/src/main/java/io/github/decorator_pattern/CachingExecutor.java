package io.github.decorator_pattern;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rty
 * @version 1.0
 * @description:
 * @date 2025/8/24 10:37
 */
public class CachingExecutor implements Executor{
    // 被装饰的执行器
    protected Executor delegate;
    // 缓存存储
    protected Map<String, Object> cache = new HashMap<>();

    public CachingExecutor(Executor delegate) {
        this.delegate = delegate;
    }

    public int getCacheSize() {
        return cache.size();
    }

    @Override
    public Object query(String queryKey) {
        // 1.先检查缓存
        if (cache.containsKey(queryKey)){
            System.out.println("[CachingExecutor] 缓存命中: " + queryKey);
            return cache.get(queryKey);
        }

        // 2.缓存未命中则委托给被装饰的执行器查询数据库
        System.out.println("[CachingExecutor] 缓存未命中，委托查询: " + queryKey);
        Object result = delegate.query(queryKey);

        // 3.将缓存放入
        cache.put(queryKey, result);
        System.out.println("[CachingExecutor] 结果已缓存: " + queryKey);

        return result;
    }

    @Override
    public int update(String queryKey) {
        // 1. 先执行更新操作
        System.out.println("[CachingExecutor] 执行更新操作: " + queryKey);
        int affectedRows = delegate.update(queryKey);

        // 2. 更新后清空相关缓存（这里简单清空所有缓存）
        System.out.println("[CachingExecutor] 更新完成，清空缓存");
        cache.clear();

        return affectedRows;
    }

    public void clearCache() {
        cache.clear();
        System.out.println("[CachingExecutor] 缓存已手动清空");
    }
}
