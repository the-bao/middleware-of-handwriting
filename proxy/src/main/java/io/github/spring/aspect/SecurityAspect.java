package io.github.spring.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * @author rty
 * @version 1.0
 * @description: 权限校验切面
 * @date 2025/8/17 21:25
 */
@Aspect
@Component
public class SecurityAspect {

    @Before("@annotation(io.github.spring.annotation.AdminOnly)")
    public void checkAdmin(JoinPoint joinPoint){
        System.out.println("[安全] 检查管理员权限...");
    }
}
