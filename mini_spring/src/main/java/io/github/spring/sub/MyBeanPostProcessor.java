package io.github.spring.sub;

import io.github.annotation.Component;
import io.github.spring.BeanPostProcessor;

/**
 * @author rty
 * @version 1.0
 * @description: TODO
 * @date 2025/8/16 16:39
 */
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object beforeInitializeBean(Object bean, String beanName) {
        System.out.println("before初始化:" + beanName);
        return bean;
    }

    @Override
    public Object afterInitializeBean(Object bean, String beanName) {
        System.out.println("after初始化:" + beanName);
        return bean;
    }
}
