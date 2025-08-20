package io.github.myproxy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author rty
 * @version 1.0
 * @description: TODO
 * @date 2025/8/20 18:35
 */
public class MyInterfaceFactory {
    private static final AtomicInteger count = new AtomicInteger();

    public static File createJavaFile(String className,MyHandler handler) throws IOException {
        String func1 = handler.functionBody("func1");
        String func2 = handler.functionBody("func2");
        String func3 = handler.functionBody("func3");
        String context = "package io.github.myproxy;\n" +
                "\n" +
                "public class "+ className +" implements MyInterface{\n" +
                "MyInterface myInterface;\n" +
                "    @Override\n" +
                "    public void func1() {\n" +
                "        "+ func1 +"\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void func2() {\n" +
                "        "+ func2 +"\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void func3() {\n" +
                "        "+ func3 +"\n" +
                "    }\n" +
                "}";

        File javaFile = new File(className+".java");
        System.out.println(javaFile.toPath());
        Files.writeString(javaFile.toPath(),context);
        return javaFile;
    }

    private static String getClassName() {
        return "MyInterface$proxy" + count.incrementAndGet();
    }

    private static MyInterface newInstance(String className,MyHandler handler) throws Exception {
        Class<?> aClass = MyInterfaceFactory.class.getClassLoader().loadClass(className);
        Constructor<?> constructor = aClass.getConstructor();
        // 创建代理对象
        MyInterface proxy = (MyInterface) constructor.newInstance();
        // 属性注入
        handler.setProxy(proxy);
        return proxy;
    }

    public static MyInterface createProxyObject(MyHandler handler) throws Exception{
        String className = getClassName();
        File javaFile = createJavaFile(className,handler);
        Compiler.compile(javaFile);
        return newInstance("io.github.myproxy."+className,handler);
    }
}
