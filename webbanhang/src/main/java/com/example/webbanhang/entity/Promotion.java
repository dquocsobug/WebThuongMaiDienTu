package com.example.webbanhang.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PromotionID")
    private Integer promotionId;

    @Column(name = "PromotionName", length = 255, nullable = false)
    private String promotionName;

    @Column(name = "DiscountPercent", nullable = false)
    private Integer discountPercent;

    @Column(name = "StartDate")
    private LocalDateTime startDate;

    @Column(name = "EndDate")
    private LocalDateTime endDate;

    // ── Relationships ────────────────────────────────────────────────────────

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductPromotion> productPromotions = new ArrayList<>();

    // Kiểm tra khuyến mãi có đang hoạt động không
    @Transient
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        if (startDate != null && now.isBefore(startDate)) return false;
        if (endDate != null && now.isAfter(endDate)) return false;
        return true;
    }
}