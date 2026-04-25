package com.example.webbanhang.dto.request;

import com.example.webbanhang.enums.VoucherType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class VoucherRequest {

    @NotBlank(message = "Mã voucher không được để trống")
    @Size(max = 50, message = "Mã voucher tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Mã voucher chỉ gồm chữ hoa, số, dấu _ và -")
    private String code;

    @NotBlank(message = "Tên voucher không được để trống")
    @Size(max = 255, message = "Tên voucher tối đa 255 ký tự")
    private String voucherName;

    @NotNull(message = "Loại voucher không được để trống")
    private VoucherType voucherType;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị giảm phải > 0")
    private BigDecimal discountValue;

    /** Chỉ áp dụng khi voucherType = PERCENT */
    @DecimalMin(value = "0.0", message = "Giảm tối đa không được âm")
    private BigDecimal maxDiscount;

    @DecimalMin(value = "0.0", message = "Giá trị đơn hàng tối thiểu không được âm")
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @Min(value = 1, message = "Giới hạn sử dụng phải >= 1")
    private Integer usageLimit;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Boolean isActive    = true;
    private Boolean isLoyalOnly = false;
}