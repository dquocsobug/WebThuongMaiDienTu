package com.example.webbanhang.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "OrderDetails")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderDetailID")
    private Integer orderDetailId;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    /**
     * Giá tại thời điểm đặt hàng (snapshot từ Products.Price).
     * Tên cột vật lý là "Price" trong DB nhưng ta ánh xạ sang unitPrice để rõ nghĩa.
     */
    @Column(name = "Price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Subtotal = unitPrice * quantity.
     * Cột này không tồn tại trong DB gốc → tính bằng @Transient hoặc thêm cột migration.
     * Ở đây dùng @Transient để không ảnh hưởng schema hiện tại.
     */
    @Transient
    public BigDecimal getSubtotal() {
        if (unitPrice == null || quantity == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // ── Relationships ────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderID", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductID", nullable = false)
    private Product product;
}