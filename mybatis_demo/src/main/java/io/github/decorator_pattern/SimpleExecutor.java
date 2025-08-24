package io.github.decorator_pattern;

/**
 * @author rty
 * @version 1.0
 * @description: 这是装饰器模式中的「具体组件」
 * @date 2025/8/24 10:36
 */
public class SimpleExecutor implements Executor{

    @Override
    public Object query(String queryKey) {
        System.out.println("[SimpleExecutor] 查询数据库: " + queryKey);
        // 模拟数据库查询耗时
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Data for: " + queryKey;
    }

    @Override
    public int update(String queryKey) {
        System.out.println("[SimpleExecutor] 更新数据库: " + queryKey);
        // 模拟数据库更新
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return 1; // 返回影响行数
    }
}
