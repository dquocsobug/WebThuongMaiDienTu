package com.example.webbanhang.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String productName;

    private String description;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @Min(value = 0, message = "Số lượng không được âm")
    private Integer stock;

    private String imageUrl;

    @NotNull(message = "Danh mục không được để trống")
    private Integer categoryId;
}