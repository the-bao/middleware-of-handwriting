package io.github.spring;

import io.github.spring.service.OrderService;
import io.github.spring.service.PaymentService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author rty
 * @version 1.0
 * @description: TODO
 * @date 2025/8/17 21:28
 */

@SpringBootApplication
public class AopDemoApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(AopDemoApplication.class, args);

        OrderService orderService = context.getBean(OrderService.class);
        PaymentService paymentService = context.getBean(PaymentService.class);

        // 测试日志和性能监控切面
        orderService.createOrder("12345");
        paymentService.pay("12345", 100.0);

        // 测试权限切面
        try {
            orderService.deleteOrder("12345");
        } catch (Exception e) {
            System.out.println("捕获异常: " + e.getMessage());
        }
    }
}
