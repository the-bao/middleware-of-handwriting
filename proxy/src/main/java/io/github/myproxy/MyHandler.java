package io.github.myproxy;

public interface MyHandler {
    public String functionBody(String methodName);

    default void setProxy(MyInterface proxy){}
}
