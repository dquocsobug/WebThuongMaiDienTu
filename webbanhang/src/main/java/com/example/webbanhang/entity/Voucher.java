package com.example.webbanhang.entity;

import com.example.webbanhang.enums.VoucherType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VoucherID")
    private Integer voucherId;

    @Column(name = "Code", length = 50, nullable = false, unique = true)
    private String code;

    @Column(name = "VoucherName", length = 255, nullable = false)
    private String voucherName;

    @Enumerated(EnumType.STRING)
    @Column(name = "VoucherType", length = 20, nullable = false)
    private VoucherType voucherType;

    /** Giá trị giảm: nếu PERCENT thì 0-100, nếu FIXED thì số tiền */
    @Column(name = "DiscountValue", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountValue;

    /** Giảm tối đa (áp dụng cho loại PERCENT) */
    @Column(name = "MaxDiscount", precision = 18, scale = 2)
    private BigDecimal maxDiscount;

    /** Giá trị đơn hàng tối thiểu để dùng voucher */
    @Column(name = "MinOrderAmount", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @Column(name = "UsageLimit")
    private Integer usageLimit;

    @Column(name = "UsedCount", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    @Column(name = "StartDate")
    private LocalDateTime startDate;

    @Column(name = "EndDate")
    private LocalDateTime endDate;

    @Column(name = "IsActive", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** Chỉ dành cho loyal_customer (true) hay tất cả (false) */
    @Column(name = "IsLoyalOnly", nullable = false)
    @Builder.Default
    private Boolean isLoyalOnly = false;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Relationships ────────────────────────────────────────────────────────

    @OneToMany(mappedBy = "voucher", fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserVoucher> userVouchers = new ArrayList<>();

    @Transient
    public boolean isValid() {
        if (!Boolean.TRUE.equals(isActive)) return false;
        LocalDateTime now = LocalDateTime.now();
        if (startDate != null && now.isBefore(startDate)) return false;
        if (endDate != null && now.isAfter(endDate)) return false;
        if (usageLimit != null && usedCount >= usageLimit) return false;
        return true;
    }
}
