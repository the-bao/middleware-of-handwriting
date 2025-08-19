package io.github.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface HandlerAdapter {
    public boolean supports(Object handler);

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;

    public long getLastModified(HttpServletRequest request, Object handler);
}
