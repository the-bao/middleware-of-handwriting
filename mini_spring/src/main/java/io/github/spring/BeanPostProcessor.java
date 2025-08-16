package io.github.spring;

/*
* 初始化bean前后的操作
* */
public interface BeanPostProcessor {

    default Object beforeInitializeBean(Object bean, String beanName){
        return bean;
    }

    default Object afterInitializeBean(Object bean, String beanName){
        return bean;
    }
}
