package io.github.spring.service;

import org.springframework.stereotype.Service;

/**
 * @author rty
 * @version 1.0
 * @description: TODO
 * @date 2025/8/17 21:11
 */
@Service
public class PaymentService {
    public boolean pay(String orderId, double amount) {
        System.out.println("支付订单: " + orderId + ", 金额: " + amount);
        return true;
    }
}
