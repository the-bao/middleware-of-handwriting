package io.github.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author rty
 * @version 1.0
 * @description:
 * @date 2025/8/17 17:58
 */
public class UserServiceInvocationHandler implements InvocationHandler {
    private Object target;

    public UserServiceInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("静态代理 - 前置处理");
        Object result = method.invoke(target,args);
        System.out.println("静态代理 - 后置处理");
        return result;
    }
}
