package io.github.decorator_pattern;

/**
 * @author rty
 * @version 1.0
 * @description:
 * @date 2025/8/24 10:41
 */
public class DecoratorPatternDemo {
    public static void main(String[] args) {
        System.out.println("=== 装饰器模式示例 - MyBatis执行器模拟 ===\n");

        // 1. 创建基础执行器
        System.out.println("1. 创建基础SimpleExecutor");
        Executor simpleExecutor = new SimpleExecutor();

        // 2. 用CachingExecutor装饰基础执行器
        System.out.println("2. 用CachingExecutor装饰SimpleExecutor");
        CachingExecutor cachingExecutor = new CachingExecutor(simpleExecutor);

        System.out.println("\n=== 第一次查询（缓存未命中）===");
        Object result1 = cachingExecutor.query("SELECT * FROM users WHERE id=1");
        System.out.println("查询结果: " + result1);
        System.out.println("当前缓存大小: " + cachingExecutor.getCacheSize());

        System.out.println("\n=== 第二次相同查询（缓存命中）===");
        Object result2 = cachingExecutor.query("SELECT * FROM users WHERE id=1");
        System.out.println("查询结果: " + result2);
        System.out.println("当前缓存大小: " + cachingExecutor.getCacheSize());

        System.out.println("\n=== 不同查询（缓存未命中）===");
        Object result3 = cachingExecutor.query("SELECT * FROM products WHERE id=5");
        System.out.println("查询结果: " + result3);
        System.out.println("当前缓存大小: " + cachingExecutor.getCacheSize());

        System.out.println("\n=== 执行更新操作（会清空缓存）===");
        cachingExecutor.update("UPDATE users SET name='John' WHERE id=1");
        System.out.println("更新后缓存大小: " + cachingExecutor.getCacheSize());

        System.out.println("\n=== 更新后再次查询（缓存未命中）===");
        Object result4 = cachingExecutor.query("SELECT * FROM users WHERE id=1");
        System.out.println("查询结果: " + result4);
        System.out.println("最终缓存大小: " + cachingExecutor.getCacheSize());
    }
}
