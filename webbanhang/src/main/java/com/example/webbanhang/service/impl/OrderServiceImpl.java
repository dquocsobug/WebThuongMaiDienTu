package com.example.webbanhang.service.impl;

import com.example.webbanhang.dto.request.PlaceOrderRequest;
import com.example.webbanhang.dto.request.UpdateOrderStatusRequest;
import com.example.webbanhang.dto.response.*;
import com.example.webbanhang.entity.*;
import com.example.webbanhang.enums.OrderStatus;
import com.example.webbanhang.enums.PaymentStatus;
import com.example.webbanhang.exception.BadRequestException;
import com.example.webbanhang.exception.ForbiddenException;
import com.example.webbanhang.exception.ResourceNotFoundException;
import com.example.webbanhang.repository.*;
import com.example.webbanhang.service.OrderService;
import com.example.webbanhang.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository        orderRepository;
    private final OrderDetailRepository  orderDetailRepository;
    private final CartRepository         cartRepository;
    private final CartItemRepository     cartItemRepository;
    private final ProductRepository      productRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository         userRepository;
    private final VoucherService         voucherService;

    // ── Mapper ────────────────────────────────────────────────────────────────

    private OrderResponse toResponse(Order order) {
        List<OrderDetail> details = orderDetailRepository
                .findByOrderIdWithProduct(order.getOrderId());

        List<OrderDetailResponse> detailResponses = details.stream().map(d -> {
            Product p = d.getProduct();
            String mainImg = productImageRepository
                    .findByProductProductIdAndIsMainTrue(p.getProductId())
                    .map(ProductImage::getImageUrl).orElse(null);

            ProductSummaryResponse pSummary = ProductSummaryResponse.builder()
                    .productId(p.getProductId())
                    .productName(p.getProductName())
                    .price(d.getUnitPrice())
                    .stock(p.getStock())
                    .mainImageUrl(mainImg)
                    .categoryName(p.getCategory().getCategoryName())
                    .build();

            BigDecimal subtotal = d.getUnitPrice()
                    .multiply(BigDecimal.valueOf(d.getQuantity()));

            return OrderDetailResponse.builder()
                    .orderDetailId(d.getOrderDetailId())
                    .product(pSummary)
                    .quantity(d.getQuantity())
                    .unitPrice(d.getUnitPrice())
                    .subtotal(subtotal)
                    .build();
        }).toList();

        User u = order.getUser();
        UserSummaryResponse userSummary = UserSummaryResponse.builder()
                .userId(u.getUserId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .build();

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .user(userSummary)
                .orderDetails(detailResponses)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .note(order.getNote())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderSummaryResponse toSummaryResponse(Order order) {
        int itemCount = orderDetailRepository.findByOrderOrderId(order.getOrderId()).size();
        return OrderSummaryResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUser().getUserId())
                .receiverName(order.getReceiverName())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .itemCount(itemCount)
                .createdAt(order.getCreatedAt())
                .build();
    }

    // ── Place Order ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public OrderResponse placeOrder(Integer userId, PlaceOrderRequest request) {

        // 1. Lấy giỏ hàng
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart của user", userId));

        if (cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Giỏ hàng trống, không thể đặt hàng");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // 2. Tạo Order
        Order order = Order.builder()
                .user(user)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .note(request.getNote())
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .totalAmount(BigDecimal.ZERO) // tính lại bên dưới
                .build();
        orderRepository.save(order);

        // 3. Tạo OrderDetails + trừ tồn kho
        BigDecimal rawTotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            int qty = cartItem.getQuantity();

            // Kiểm tra tồn kho
            int updated = productRepository.decreaseStock(product.getProductId(), qty);
            if (updated == 0) {
                throw new BadRequestException(
                        "Sản phẩm '" + product.getProductName() + "' không đủ tồn kho");
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal  = unitPrice.multiply(BigDecimal.valueOf(qty));
            rawTotal = rawTotal.add(subtotal);

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(qty)
                    .unitPrice(unitPrice)
                    .build();
            orderDetailRepository.save(detail);
        }

        // 4. Áp dụng Voucher nếu có
        BigDecimal finalTotal = rawTotal;
        if (StringUtils.hasText(request.getVoucherCode())) {
            BigDecimal discount = voucherService.calculateDiscount(
                    userId, request.getVoucherCode(), rawTotal);
            finalTotal = rawTotal.subtract(discount).max(BigDecimal.ZERO);
            voucherService.markVoucherUsed(userId, request.getVoucherCode());
        }

        // 5. Cập nhật tổng tiền
        order.setTotalAmount(finalTotal);
        orderRepository.save(order);

        // 6. Xóa giỏ hàng
        cartItemRepository.deleteAllByCartId(cart.getCartId());

        log.info("[Order] User {} đặt hàng thành công, orderId={}, total={}",
                userId, order.getOrderId(), finalTotal);

        return toResponse(order);
    }

    // ── User ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getMyOrders(Integer userId, Pageable pageable) {
        Page<Order> page = orderRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable);
        List<OrderSummaryResponse> content = page.getContent().stream()
                .map(this::toSummaryResponse).toList();
        return PageResponse.of(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(Integer userId, Integer orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (!order.getUser().getUserId().equals(userId)) {
            throw new ForbiddenException("Bạn không có quyền xem đơn hàng này");
        }
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Integer userId, Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (!order.getUser().getUserId().equals(userId)) {
            throw new ForbiddenException("Bạn không có quyền huỷ đơn hàng này");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể huỷ đơn hàng đang ở trạng thái PENDING");
        }

        // Hoàn lại tồn kho
        List<OrderDetail> details = orderDetailRepository.findByOrderOrderId(orderId);
        details.forEach(d ->
                productRepository.increaseStock(d.getProduct().getProductId(), d.getQuantity()));

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("[Order] User {} huỷ đơn orderId={}", userId, orderId);
        return toResponse(order);
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getAllOrders(Integer userId,
                                                           OrderStatus status,
                                                           LocalDateTime fromDate,
                                                           LocalDateTime toDate,
                                                           Pageable pageable) {
        Page<Order> page = orderRepository.findWithFilters(userId, status, fromDate, toDate, pageable);
        List<OrderSummaryResponse> content = page.getContent().stream()
                .map(this::toSummaryResponse).toList();
        return PageResponse.of(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse adminGetOrderDetail(Integer orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Integer orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        validateStatusTransition(order.getStatus(), request.getStatus());

        // Nếu CANCELLED từ trạng thái chưa giao → hoàn kho
        if (request.getStatus() == OrderStatus.CANCELLED
                && order.getStatus() != OrderStatus.DELIVERED) {
            List<OrderDetail> details = orderDetailRepository.findByOrderOrderId(orderId);
            details.forEach(d ->
                    productRepository.increaseStock(d.getProduct().getProductId(), d.getQuantity()));
        }

        // Nếu DELIVERED → cập nhật PaymentStatus nếu COD
        if (request.getStatus() == OrderStatus.DELIVERED
                && "COD".equalsIgnoreCase(order.getPaymentMethod())) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        if (StringUtils.hasText(request.getNote())) {
            order.setNote(request.getNote());
        }

        order.setStatus(request.getStatus());
        orderRepository.save(order);
        log.info("[Order] Admin cập nhật orderId={} → status={}", orderId, request.getStatus());
        return toResponse(order);
    }

    // ── Validate status transition ────────────────────────────────────────────

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING   -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.SHIPPING  || next == OrderStatus.CANCELLED;
            case SHIPPING  -> next == OrderStatus.DELIVERED || next == OrderStatus.CANCELLED;
            case DELIVERED, CANCELLED -> false; // trạng thái cuối, không cho chuyển
        };
        if (!valid) {
            throw new BadRequestException(
                    "Không thể chuyển trạng thái từ " + current + " sang " + next);
        }
    }
}