package com.example.webbanhang.controller;

import com.example.webbanhang.dto.request.PromotionRequest;
import com.example.webbanhang.dto.response.PromotionResponse;
import com.example.webbanhang.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    /**
     * GET /api/promotions       - Public: tất cả khuyến mãi
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(promotionService.getAll()));
    }

    /**
     * GET /api/promotions/active - Public: khuyến mãi đang hoạt động
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getActive() {
        return ResponseEntity.ok(ApiResponse.success(promotionService.getActive()));
    }

    /**
     * GET /api/promotions/product/{productId} - Public: khuyến mãi của sản phẩm
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getByProduct(
            @PathVariable Integer productId) {
        return ResponseEntity.ok(ApiResponse.success(promotionService.getByProduct(productId)));
    }

    /**
     * POST /api/promotions      - ADMIN: tạo khuyến mãi
     * Body: { promotionName, discountPercent, startDate, endDate }
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromotionResponse>> create(
            @Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tạo khuyến mãi thành công",
                promotionService.create(request)));
    }

    /**
     * PUT /api/promotions/{id}  - ADMIN: cập nhật khuyến mãi
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromotionResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công",
                promotionService.update(id, request)));
    }

    /**
     * DELETE /api/promotions/{id} - ADMIN: xóa khuyến mãi
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        promotionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa khuyến mãi thành công", null));
    }

    /**
     * POST /api/promotions/{promotionId}/apply/{productId}
     * ADMIN: áp dụng khuyến mãi cho sản phẩm
     */
    @PostMapping("/{promotionId}/apply/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> applyToProduct(
            @PathVariable Integer promotionId,
            @PathVariable Integer productId) {
        promotionService.applyToProduct(productId, promotionId);
        return ResponseEntity.ok(ApiResponse.success("Áp dụng khuyến mãi thành công", null));
    }

    /**
     * DELETE /api/promotions/{promotionId}/remove/{productId}
     * ADMIN: gỡ khuyến mãi khỏi sản phẩm
     */
    @DeleteMapping("/{promotionId}/remove/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeFromProduct(
            @PathVariable Integer promotionId,
            @PathVariable Integer productId) {
        promotionService.removeFromProduct(productId, promotionId);
        return ResponseEntity.ok(ApiResponse.success("Đã gỡ khuyến mãi khỏi sản phẩm", null));
    }
}