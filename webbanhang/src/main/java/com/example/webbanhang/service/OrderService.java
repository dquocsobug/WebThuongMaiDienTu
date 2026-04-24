package com.example.webbanhang.service;

import com.example.webbanhang.dto.response.OrderResponse;
import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(Integer userId);
    OrderResponse getById(Integer orderId);
    List<OrderResponse> getByUser(Integer userId);
    List<OrderResponse> getAll();
    OrderResponse updateStatus(Integer orderId, String status);
}