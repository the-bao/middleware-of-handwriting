package io.github.spring.sub;

import io.github.annotation.Autowired;
import io.github.annotation.Component;
import io.github.annotation.PostConstruct;

/**
 * @author rty
 * @version 1.0
 * @description: TODO
 * @date 2025/8/16 1:23
 */
@Component
public class TestService {

    @Autowired
    Test test;

    @PostConstruct
    public void init(){
        System.out.println("TestService 创建了：" + test);
    }
}
