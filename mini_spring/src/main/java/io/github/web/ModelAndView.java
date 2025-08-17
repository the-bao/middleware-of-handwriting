package io.github.web;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rty
 * @version 1.0
 * @description:
 * @date 2025/8/17 15:07
 */
public class ModelAndView {
    String view;

    Map<String,String> context = new HashMap<>();

    public Map<String, String> getContext() {
        return context;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getView() {
        return view;
    }
}
