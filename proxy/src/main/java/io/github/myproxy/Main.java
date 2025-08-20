package io.github.myproxy;

import java.lang.reflect.Field;

/**
 * @author rty
 * @version 1.0
 * @description: TODO
 * @date 2025/8/20 20:30
 */
public class Main {
    public static void main(String[] args) throws Exception {
        MyInterface myInterface = MyInterfaceFactory.createProxyObject(new PrintFunctionName());
        myInterface.func1();
        myInterface.func2();
        myInterface.func3();
        System.out.println("======================================");
        myInterface = MyInterfaceFactory.createProxyObject(new PrintFunctionName2());
        myInterface.func1();
        myInterface.func2();
        myInterface.func3();
        System.out.println("======================================");
        myInterface = MyInterfaceFactory.createProxyObject(new PrintFunctionName3(myInterface));
        myInterface.func1();
        myInterface.func2();
        myInterface.func3();
    }

    static class PrintFunctionName implements MyHandler{
        @Override
        public String functionBody(String methodName) {
            return "System.out.println(\""+ methodName +"\");";
        }
    }

    static class PrintFunctionName2 implements MyHandler{
        @Override
        public String functionBody(String methodName) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("System.out.println(\"1\");")
                    .append("System.out.println(\""+ methodName +"\");");

            return stringBuilder.toString();
        }
    }

    static class PrintFunctionName3 implements MyHandler{
        MyInterface myInterface;

        public PrintFunctionName3(MyInterface myInterface) {
            this.myInterface = myInterface;
        }

        @Override
        public void setProxy(MyInterface proxy) {
            Class<? extends MyInterface> aClass = proxy.getClass();
            Field field = null;
            try {
                field = aClass.getDeclaredField("myInterface");
                field.setAccessible(true);
                field.set(proxy,myInterface);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String functionBody(String methodName) {
            String context = "        System.out.println(\"before\");\n" +
                    "        myInterface."+ methodName +"();\n" +
                    "        System.out.println(\"after\");";

            return context;
        }
    }
}
