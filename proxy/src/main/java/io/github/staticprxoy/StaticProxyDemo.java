package io.github.staticprxoy;

/**
 * @author rty
 * @version 1.0
 * @description: TODO
 * @date 2025/8/17 17:56
 */
public class StaticProxyDemo {
    public static void main(String[] args) {
        UserService target = new UserServiceImpl();
        UserService proxy = new UserServiceStaticProxy(target);
        proxy.saveUser("test");
    }
}
