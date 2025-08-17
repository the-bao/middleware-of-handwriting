package io.github.cglib;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author rty
 * @version 1.0
 * @description: cglib动态代理实现类
 * @date 2025/8/17 18:30
 */
public class UserServiceInterceptor implements MethodInterceptor {
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("CGLIB代理 - 方法调用前: " + method.getName());

        // 调用父类(原始类)的方法
        Object result = methodProxy.invokeSuper(o, objects);

        System.out.println("CGLIB代理 - 方法调用后: " + method.getName());
        return result;
    }
}
