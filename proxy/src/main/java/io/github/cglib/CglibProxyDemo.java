package io.github.cglib;

import net.sf.cglib.proxy.Enhancer;

/**
 * @author rty
 * @version 1.0
 * @description:
 * @date 2025/8/17 18:32
 */
public class CglibProxyDemo {
    public static void main(String[] args) {
        Enhancer enhancer = new Enhancer();

        // 设置父类 （代理对象）
        enhancer.setSuperclass(UserServiceImpl.class);

        // 设置回调（代理实现）
        enhancer.setCallback(new UserServiceInterceptor());

        UserService proxy = (UserService) enhancer.create();

        proxy.saveUser("test");
    }
}
