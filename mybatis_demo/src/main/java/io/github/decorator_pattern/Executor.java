package io.github.decorator_pattern;

/**
 * 执行器接口 - 对应MyBatis的Executor
 * 这是装饰器模式中的「组件」接口
 */
public interface Executor {
    Object query(String queryKey);

    int update(String queryKey);
}
