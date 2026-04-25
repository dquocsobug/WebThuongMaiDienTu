package com.example.webbanhang.controller;

import com.example.webbanhang.dto.request.ProductRequest;
import com.example.webbanhang.dto.response.ProductResponse;
import com.example.webbanhang.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/products              - Public: tất cả sản phẩm
     * GET /api/products?keyword=áo   - Public: tìm kiếm theo tên
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAll(
            @RequestParam(required = false) String keyword) {
        List<ProductResponse> data = (keyword != null && !keyword.isBlank())
                ? productService.search(keyword)
                : productService.getAll();
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * GET /api/products/{id}         - Public: chi tiết sản phẩm
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getById(id)));
    }

    /**
     * GET /api/products/category/{categoryId} - Public: sản phẩm theo danh mục
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getByCategory(
            @PathVariable Integer categoryId) {
        return ResponseEntity.ok(ApiResponse.success(productService.getByCategory(categoryId)));
    }

    /**
     * POST /api/products             - ADMIN: thêm sản phẩm
     * Body: { productName, description, price, stock, imageUrl, categoryId }
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Thêm sản phẩm thành công",
                productService.create(request)));
    }

    /**
     * PUT /api/products/{id}         - ADMIN: cập nhật sản phẩm
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật sản phẩm thành công",
                productService.update(id, request)));
    }

    /**
     * DELETE /api/products/{id}      - ADMIN: xóa sản phẩm
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        productService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm thành công", null));
    }
}