package io.github.web.controller;

import io.github.annotation.*;
import io.github.web.ModelAndView;
import io.github.web.vo.User;

import java.util.Map;

/**
 * @author rty
 * @version 1.0
 * @description:
 * @date 2025/8/17 11:16
 */
@Component
@Controller
@RequestMapping("/hello")
public class HelloController {

    @RequestMapping("/a")
    public String hello(@Param("name") String name, @Param("age") Integer age){
        return String.format("<h1>Hello %s, Age:%s</h1>",name,age);
    }

    @RequestMapping("/json")
    @ResponseBody("json")
    public User json(@Param("name") String name, @Param("age") Integer age){
        return new User(name,age);
    }

    @RequestMapping("/html")
    public ModelAndView modelAndView(@Param("name") String name, @Param("age") Integer age){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setView("index.html");
        modelAndView.getContext().put("name",name);
        modelAndView.getContext().put("age",age.toString());
        return modelAndView;
    }
}
