package io.github.spring.service;

import io.github.spring.annotation.AdminOnly;
import org.springframework.stereotype.Service;

/**
 * @author rty
 * @version 1.0
 * @description:
 * @date 2025/8/17 21:09
 */
@Service
public class OrderService {
    public String createOrder(String orderId){
        System.out.println("创建订单：" + orderId);
        return "订单" + orderId + "创建成功";
    }

    public void cancelOrder(String orderId) {
        System.out.println("取消订单: " + orderId);
    }

    @AdminOnly
    public void deleteOrder(String orderId) {
        System.out.println("删除订单: " + orderId);
    }
}
