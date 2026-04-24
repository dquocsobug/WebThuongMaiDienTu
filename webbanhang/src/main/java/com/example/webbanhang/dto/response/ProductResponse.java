package com.example.webbanhang.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class ProductResponse {
    private Integer productId;
    private String productName;
    private String description;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private Integer discountPercent;
    private Integer stock;
    private String imageUrl;
    private Integer categoryId;
    private String categoryName;
    private Double avgRating;
    private LocalDateTime createdAt;
}