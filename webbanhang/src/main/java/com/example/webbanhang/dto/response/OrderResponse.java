package com.example.webbanhang.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class OrderResponse {
    private Integer orderId;
    private Integer userId;
    private String userFullName;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private List<OrderDetailResponse> details;

    @Data @AllArgsConstructor @NoArgsConstructor @Builder
    public static class OrderDetailResponse {
        private Integer orderDetailId;
        private Integer productId;
        private String productName;
        private String imageUrl;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;
    }
}