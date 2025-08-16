package io.github.spring.sub;

import io.github.annotation.Autowired;
import io.github.annotation.Component;
import io.github.annotation.PostConstruct;

/**
 * @author rty
 * @version 1.0
 * @description: TODO
 * @date 2025/8/16 0:50
 */
@Component(name = "test")
public class Test {

    @Autowired
    private TestService testService;

    @PostConstruct
    public void init(){
        System.out.println("Test 创建了：" + testService);
    }
}
