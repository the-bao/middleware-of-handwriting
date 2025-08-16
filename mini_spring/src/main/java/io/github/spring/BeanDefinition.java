package io.github.spring;

import io.github.annotation.Autowired;
import io.github.annotation.Component;
import io.github.annotation.PostConstruct;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @author rty
 * @version 1.0
 * @description: TODO
 * @date 2025/8/15 23:13
 */
public class BeanDefinition {

    private final String name;
    private final Constructor<?> constructor;
    // TODO 实现允许执行多个PostConstruct
    // spring中允许容器实例化后执行多个PostConstruct方法。但是不推荐，建议将初始化逻辑集中。并且多个方法的执行顺序是随机的，除非通过dependon去指定
    private final Method postConstructMethod;
    private final List<Field> autowiredFields;
    private final Class<?> beanType;

    public BeanDefinition(Class<?> type){
        this.beanType = type;
        Component component = type.getDeclaredAnnotation(Component.class);
        this.name = component.name().isEmpty() ? type.getSimpleName() : component.name();
        try {
            this.constructor = type.getConstructor();

            this.postConstructMethod = Arrays.stream(type.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(PostConstruct.class)).findFirst().orElse(null);

            this.autowiredFields = Arrays.stream(type.getDeclaredFields()).filter( f -> f.isAnnotationPresent(Autowired.class)).toList();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName(){
        return name;
    }

    public Constructor<?> getConstructor(){
        return constructor;
    }

    public Class<?> getBeanType() {
        return beanType;
    }

    /*
    * 获取容器中带有@PostConstruct注解的函数
    * */
    public Method getPostConstructMethod(){
        return postConstructMethod;
    }

    public List<Field> getAutowiredFields(){
        return autowiredFields;
    }
}
