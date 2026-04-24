package com.example.webbanhang.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemRequest {
    @NotNull(message = "Sản phẩm không được để trống")
    private Integer productId;

    @Min(value = 1, message = "Số lượng phải ít nhất là 1")
    private Integer quantity = 1;
}