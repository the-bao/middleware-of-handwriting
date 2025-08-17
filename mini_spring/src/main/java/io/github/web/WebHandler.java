package io.github.web;

import io.github.annotation.ResponseBody;

import java.lang.reflect.Method;

/**
 * @author rty
 * @version 1.0
 * @description: 封装请求实例
 * @date 2025/8/17 11:25
 */
public class WebHandler {

    private final Object controllerBean;

    private final Method method;

    private final ResultType resultType;

    public WebHandler(Object controllerBean, Method method) {
        this.controllerBean = controllerBean;
        this.method = method;
        this.resultType = resolveResultType(controllerBean,method);
    }

    private ResultType resolveResultType(Object controllerBean, Method method) {
        // 带有注解，按照json返回
        if (method.isAnnotationPresent(ResponseBody.class)){
            ResponseBody responseBody = method.getDeclaredAnnotation(ResponseBody.class);
            return ResultType.JSON;
        }

        // 静态资源
        if (method.getReturnType() == ModelAndView.class){
            return ResultType.LOCAL;
        }

        // 默认以html格式返回
        return ResultType.HTML;
    }

    public Object getControllerBean() {
        return controllerBean;
    }

    public Method getMethod() {
        return method;
    }

    public ResultType getResultType() {
        return resultType;
    }

    enum ResultType{
        JSON, HTML, LOCAL
    }
}
