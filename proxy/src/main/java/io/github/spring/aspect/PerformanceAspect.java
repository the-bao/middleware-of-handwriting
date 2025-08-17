package io.github.spring.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author rty
 * @version 1.0
 * @description: 监控切面
 * @date 2025/8/17 21:22
 */
@Aspect
@Component
public class PerformanceAspect {
    @Around("execution(* io.github.spring.service.*.*(..))")
    public Object measureExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            Object result = pjp.proceed();
            return result;
        }finally {
            long executionTime = System.currentTimeMillis() - startTime;
            System.out.println("[性能] " + pjp.getSignature() +
                    " 执行耗时: " + executionTime + "ms");
        }
    }
}
