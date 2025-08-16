package io.github.spring;

import io.github.Main;
import io.github.annotation.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rty
 * @version 1.0
 * @description: TODO
 * @date 2025/8/15 22:57
 */

/*
* spring 管理容器 简单说就是 造对象 和 拿对象
* bean -> beanDefinition -> 实例化 -> 依赖注入 -> 初始化方法调用
*                             |                     |
*                              |  --   初始化   -- |
* */
public class ApplicationContext {

    // 存放容器的定义，为后续bean初始化提供信息
    // 由于需要进行依赖注入，所以不能直接初始化bean，而是将所有的BeanDefinition存放起来，方便依赖注入时初始化依赖
    // A -> B A初始化依赖B，但是初始化时顺序是不固定的，可能A先初始化B再初始化，所以将B初始化需要的条件放在了beanDefinitionMap中。缺少B时直接将B初始化再注入
    private Map<String,BeanDefinition> beanDefinitionMap = new HashMap<>();

    // 存放着已经初始化后的bean
    private Map<String,Object> ioc = new HashMap<>();

    // 多增加一级缓存 用于解决循环依赖的问题 存放实例化了，但是没有初始化的bean
    private Map<String,Object> loadingIoc = new HashMap<>();

    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public ApplicationContext(String packageName) throws Exception {
        initContext(packageName);
    }

    public Object getBean(String name){
        if (name == null) return null;
        Object bean = ioc.get(name);
        if (bean != null){
            return bean;
        }
        // 如果bean还初始化 则拿到beanDefinition进行初始化
        if (beanDefinitionMap.containsKey(name)){
            return createBean(beanDefinitionMap.get(name));
        }
        return null;
    }

    public <T> T getBean(Class<T> beanType){
        String name = this.beanDefinitionMap.values().stream()
                .filter(beanDefinition -> beanType.isAssignableFrom(beanDefinition.getBeanType()))
                .map(BeanDefinition::getName)
                .findFirst()
                .orElse(null);
        return (T) getBean(name);
    }

    public <T> List<T> getBeans(Class<T> beanType){
        return this.beanDefinitionMap.values().stream()
                .filter(beanDefinition -> beanType.isAssignableFrom(beanDefinition.getBeanType()))
                .map(BeanDefinition::getName)
                .map(this::getBean)
                .map(bean -> (T)bean)
                .toList();
    }

    /*
    * 1.造什么对象
    * 2.怎么造 （主要是解决构造函数里的参数从哪里获取、属性如何注入、生命周期怎么执行）
    * */
    public void initContext(String packageName) throws Exception {
        // 扫描 package 把所有的bean放入BeanDefinitionMap中
        // 初始化时分开，防止初始化bean的时需要依赖注入，但是依赖还没加入到BeanDefinitionMap中
        scanPackage(packageName).stream().filter(this::scanCreate).forEach(this::wrapper);

        // 初始化 BeanPostProcessor
        initBeanPostProcessor();

        // 初始化bean
        beanDefinitionMap.values().forEach(this::createBean);
    }

    private void initBeanPostProcessor() {
        beanDefinitionMap.values().stream()
                .filter(beanDefinition -> BeanPostProcessor.class.isAssignableFrom(beanDefinition.getBeanType()))
                .map(this::createBean)
                .map(bean -> (BeanPostProcessor)bean)
                .forEach(beanPostProcessorList::add);
    }

    protected Object createBean(BeanDefinition beanDefinition){
        String name = beanDefinition.getName();
        if (ioc.containsKey(name)) return ioc.get(name);

        if (loadingIoc.containsKey(name)){
            return loadingIoc.get(name);
        }
        return doCreateBean(beanDefinition);
    }

    private Object doCreateBean(BeanDefinition beanDefinition) {
        Constructor<?> constructor = beanDefinition.getConstructor();
        Object bean = null;
        try {
            // 实例化
            bean = constructor.newInstance();
            loadingIoc.put(beanDefinition.getName(),bean);

            // 自动注入属性
            autowiredBean(bean,beanDefinition);

            bean = initializeBean(bean,beanDefinition);

            loadingIoc.remove(beanDefinition.getName());
            ioc.put(beanDefinition.getName(),bean);
        } catch (Exception e) {
            throw new RuntimeException("bean初始化失败",e);
        }
        return bean;
    }

    private Object initializeBean(Object bean, BeanDefinition beanDefinition) throws Exception {
        for (BeanPostProcessor beanPostProcessor:beanPostProcessorList){
            bean = beanPostProcessor.beforeInitializeBean(bean,beanDefinition.getName());
        }

        // 自动执行自定义的初始化逻辑
        Method postConstructMethod = beanDefinition.getPostConstructMethod();
        if (postConstructMethod != null){
            postConstructMethod.invoke(bean);
        }

        for (BeanPostProcessor beanPostProcessor:beanPostProcessorList){
            bean = beanPostProcessor.afterInitializeBean(bean,beanDefinition.getName());
        }

        return bean;
    }

    private void autowiredBean(Object bean, BeanDefinition beanDefinition) throws IllegalAccessException {
        for (Field autowiredField : beanDefinition.getAutowiredFields()){
            // 给autowired自动赋值
            autowiredField.setAccessible(true);
            autowiredField.set(bean,getBean(autowiredField.getType()));
        }
    }

    /*
     * 将类信息包装为BeanDefinition对象
     * 如果已经存在重复名字的就抛出异常
     * */
    protected BeanDefinition wrapper(Class<?> type){
        BeanDefinition beanDefinition = new BeanDefinition(type);
        if (beanDefinitionMap.containsKey(beanDefinition.getName())){
            throw new RuntimeException("bean名称重复");
        }
        beanDefinitionMap.put(beanDefinition.getName(),beanDefinition);
        return beanDefinition;
    }

    /*
    * 定义protected方法 将方法暴露给子类，方便通过重写方法实现自己的扫描逻辑
    * */
    protected boolean scanCreate(Class<?> type){
        return type.isAnnotationPresent(Component.class);
    }

    /*
    * 扫描package 返回package下所有的类信息
    * */
    private List<Class<?>> scanPackage(String packageName) throws Exception {
        List<Class<?>> classList = new ArrayList<>();
        // a.b.c
        URL resource = this.getClass().getClassLoader().getResource(packageName.replace(".",
                File.separator));

        Path path = Paths.get(resource.toURI());
        Files.walkFileTree(path, new SimpleFileVisitor<>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path absolutePath = file.toAbsolutePath();
                if (absolutePath.toString().endsWith(".class")){
                    String replaceStr = absolutePath.toString().replace(File.separator,".");
                    int packageIndex = replaceStr.indexOf(packageName);
                    String className = replaceStr.substring(packageIndex,replaceStr.length() - ".class".length());
                    try {
                        classList.add(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return classList;
    }
}
