package io.github.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface HandlerInterceptor {
    /**
     * 在控制器方法执行前调用
     * @return true 继续执行链，false 中断请求
     */
    default boolean preHandle(HttpServletRequest request,
                              HttpServletResponse response,
                              Object handler) throws Exception {
        return true;
    }

    /**
     * 在控制器方法执行后、视图渲染前调用
     * （注意：如果控制器方法抛出异常，则不会执行此方法）
     */
    default void postHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler,
                            ModelAndView modelAndView) throws Exception {
    }

    /**
     * 在整个请求完成后调用（视图渲染完毕）
     * 无论是否抛出异常都会执行，适合资源清理
     */
    default void afterCompletion(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object handler,
                                 Exception ex) throws Exception {
    }
}
