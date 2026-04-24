package com.example.webbanhang.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class CartResponse {
    private Integer cartId;
    private Integer userId;
    private List<CartItemResponse> items;
    private BigDecimal totalPrice;

    @Data @AllArgsConstructor @NoArgsConstructor @Builder
    public static class CartItemResponse {
        private Integer cartItemId;
        private Integer productId;
        private String productName;
        private String imageUrl;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;
    }
}