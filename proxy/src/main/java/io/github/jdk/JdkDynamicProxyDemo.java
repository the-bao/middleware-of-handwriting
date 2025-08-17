package io.github.jdk;

import java.lang.reflect.Proxy;

/**
 * @author rty
 * @version 1.0
 * @description:
 * @date 2025/8/17 18:00
 */
public class JdkDynamicProxyDemo {
    public static void main(String[] args) {
        // 确保参数生效
        System.setProperty("jdk.proxy.ProxyGenerator.saveGeneratedFiles", "true");
        // 打印确认
        System.out.println("参数状态: " +
                System.getProperty("jdk.proxy.ProxyGenerator.saveGeneratedFiles"));
        UserService target = new UserServiceImpl();

        // 通过 Proxy.newProxyInstance 动态生成代理类的字节码，代理类调用所有的方法都会使用invoke方法，增强了invoke方法就增强了代理类的所有方法
        // 但是只能代理接口：JDK动态代理只能基于接口实现，不能代理类
        // 因为生成的代理类 public final class $Proxy0 extends Proxy implements UserService 会继承Proxy ，如果代理类就违法了单一继承
        UserService proxy = (UserService) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new UserServiceInvocationHandler(target)
        );

        proxy.saveUser("test");
    }

}

