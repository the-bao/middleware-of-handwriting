package io.github.spring.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author rty
 * @version 1.0
 * @description: 日志切面
 * @date 2025/8/17 21:16
 */
@Aspect
@Component
public class LoggingAspect {
    @Pointcut("execution(* io.github.spring.service.*.*(..))")
    public void serviceLayer(){}

    @Before("serviceLayer()")
    public void logBefore(JoinPoint joinPoint){
        System.out.println("[日志] 准备执行方法: " +
                joinPoint.getSignature().getName() +
                ", 参数: " + Arrays.toString(joinPoint.getArgs()));
    }

    @After("serviceLayer()")
    public void logAfter(JoinPoint joinPoint) {
        System.out.println("[日志] 方法执行完成: " + joinPoint.getSignature().getName());
    }

    // 返回通知
    @AfterReturning(pointcut = "serviceLayer()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        System.out.println("[日志] 方法返回结果: " + result);
    }

    // 异常通知
    @AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
        System.out.println("[日志] 方法抛出异常: " + ex.getMessage());
    }
}
