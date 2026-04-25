package com.example.webbanhang.dto.response;

import com.example.webbanhang.enums.VoucherType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class VoucherResponse {

    private final Integer       voucherId;
    private final String        code;
    private final String        voucherName;
    private final VoucherType   voucherType;
    private final BigDecimal    discountValue;
    private final BigDecimal    maxDiscount;
    private final BigDecimal    minOrderAmount;
    private final Integer       usageLimit;
    private final Integer       usedCount;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final Boolean       isActive;
    private final Boolean       isLoyalOnly;
    private final boolean       valid;          // Tính tại service
    private final LocalDateTime createdAt;
}