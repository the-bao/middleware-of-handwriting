package io.github.staticprxoy;

/**
 * @author rty
 * @version 1.0
 * @description: 代理类
 * @date 2025/8/17 17:54
 */
public class UserServiceStaticProxy implements UserService{
    private UserService target;

    public UserServiceStaticProxy(UserService target) {
        this.target = target;
    }

    @Override
    public void saveUser(String name) {
        System.out.println("静态代理 - 前置处理");
        target.saveUser(name);
        System.out.println("静态代理 - 后置处理");
    }
}
