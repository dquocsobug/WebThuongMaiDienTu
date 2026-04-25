package com.example.webbanhang.service;

import com.example.webbanhang.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(Integer userId);
    CartResponse addItem(Integer userId, CartItemRequest request);
    CartResponse updateItem(Integer userId, Integer cartItemId, Integer quantity);
    CartResponse removeItem(Integer userId, Integer cartItemId);
    void clearCart(Integer userId);
}