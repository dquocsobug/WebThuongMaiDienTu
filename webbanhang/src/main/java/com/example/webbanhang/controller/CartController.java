package com.example.webbanhang.controller;

import com.example.webbanhang.config.SecurityUtils;
import com.example.webbanhang.dto.response.CartResponse;
import com.example.webbanhang.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * GET /api/cart           - Xem giỏ hàng của user hiện tại
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        Integer userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(userId)));
    }

    /**
     * POST /api/cart/items    - Thêm sản phẩm vào giỏ
     * Body: { productId, quantity }
     */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @Valid @RequestBody CartItemRequest request) {
        Integer userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Thêm vào giỏ hàng thành công",
                cartService.addItem(userId, request)));
    }

    /**
     * PUT /api/cart/items/{cartItemId}?quantity=3  - Cập nhật số lượng
     */
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @PathVariable Integer cartItemId,
            @RequestParam Integer quantity) {
        Integer userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Cập nhật giỏ hàng thành công",
                cartService.updateItem(userId, cartItemId, quantity)));
    }

    /**
     * DELETE /api/cart/items/{cartItemId} - Xóa 1 sản phẩm khỏi giỏ
     */
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable Integer cartItemId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Đã xóa sản phẩm khỏi giỏ hàng",
                cartService.removeItem(userId, cartItemId)));
    }

    /**
     * DELETE /api/cart        - Xóa toàn bộ giỏ hàng
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        Integer userId = SecurityUtils.getCurrentUserId();
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa toàn bộ giỏ hàng", null));
    }
}