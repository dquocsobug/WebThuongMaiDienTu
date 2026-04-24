package com.example.webbanhang.controller;

import com.example.webbanhang.config.SecurityUtils;
import com.example.webbanhang.dto.response.ApiResponse;
import com.example.webbanhang.dto.response.OrderResponse;
import com.example.webbanhang.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/orders        - Đặt hàng từ giỏ hàng hiện tại
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder() {
        Integer userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Đặt hàng thành công",
                orderService.placeOrder(userId)));
    }

    /**
     * GET /api/orders/my      - User: xem lịch sử đơn hàng của mình
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders() {
        Integer userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(orderService.getByUser(userId)));
    }

    /**
     * GET /api/orders/{id}    - User xem đơn của mình / Admin xem bất kỳ
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(@PathVariable Integer id) {
        OrderResponse order = orderService.getById(id);

        // User chỉ được xem đơn của chính mình
        if (!SecurityUtils.hasRole("ADMIN")) {
            Integer userId = SecurityUtils.getCurrentUserId();
            if (!order.getUserId().equals(userId)) {
                throw new SecurityException("Bạn không có quyền xem đơn hàng này");
            }
        }

        return ResponseEntity.ok(ApiResponse.success(order));
    }

    /**
     * GET /api/orders/all     - ADMIN: xem tất cả đơn hàng
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(orderService.getAll()));
    }

    /**
     * PUT /api/orders/{id}/status  - ADMIN: cập nhật trạng thái đơn hàng
     * Body: { status } — Pending | Confirmed | Shipping | Completed | Cancelled
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Integer id,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công",
                orderService.updateStatus(id, status)));
    }
}