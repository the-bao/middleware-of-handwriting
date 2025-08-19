package io.github.web;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rty
 * @version 1.0
 * @description: 拦截器链和handler的组合
 * @date 2025/8/17 23:43
 */
public class HandlerExecutionChain {
    // 实际处理器
    private final Object handler;
    // 拦截器数组
    private int interceptorIndex;
    // 拦截器列表
    private List<HandlerInterceptor> interceptorList;

    public HandlerExecutionChain(Object handler){
        this.handler = handler;
        this.interceptorList = new ArrayList();
        this.interceptorIndex = -1;
    }

    public void addInterceptor(HandlerInterceptor interceptor){
        interceptorIndex++;
        interceptorList.add(interceptor);
    }

    boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        for(int i = 0; i < this.interceptorList.size(); this.interceptorIndex = i++) {
            HandlerInterceptor interceptor = (HandlerInterceptor)this.interceptorList.get(i);
            if (!interceptor.preHandle(request, response, this.handler)) {
                this.triggerAfterCompletion(request, response, (Exception)null);
                return false;
            }
        }

        return true;
    }

    void applyPostHandle(HttpServletRequest request, HttpServletResponse response, @Nullable ModelAndView mv) throws Exception {
        for(int i = this.interceptorList.size() - 1; i >= 0; --i) {
            HandlerInterceptor interceptor = (HandlerInterceptor)this.interceptorList.get(i);
            interceptor.postHandle(request, response, this.handler, mv);
        }

    }

    void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response, @Nullable Exception ex) {
        for(int i = this.interceptorIndex; i >= 0; --i) {
            HandlerInterceptor interceptor = (HandlerInterceptor)this.interceptorList.get(i);

            try {
                interceptor.afterCompletion(request, response, this.handler, ex);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
