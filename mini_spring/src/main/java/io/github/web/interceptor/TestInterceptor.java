package io.github.web.interceptor;

import io.github.annotation.Component;
import io.github.annotation.Interceptor;
import io.github.web.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author rty
 * @version 1.0
 * @description: 自定义拦截器
 * @date 2025/8/20 0:10
 */
@Interceptor(value = {"/hello/a","/hello/json"})
@Component
public class TestInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("进入TestInterceptor拦截器的preHandle方法");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("进入TestInterceptor拦截器的afterCompletion方法");
    }
}
