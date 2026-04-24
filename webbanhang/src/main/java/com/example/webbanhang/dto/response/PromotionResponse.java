package com.example.webbanhang.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class PromotionResponse {
    private Integer promotionId;
    private String promotionName;
    private Integer discountPercent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
}