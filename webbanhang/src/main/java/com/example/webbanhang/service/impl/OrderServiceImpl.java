package com.example.webbanhang.service.impl;

import com.example.webbanhang.dto.response.OrderResponse;
import com.example.webbanhang.entity.*;
import com.example.webbanhang.repository.*;
import com.example.webbanhang.service.CartService;
import com.example.webbanhang.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    @Override
    @Transactional
    public OrderResponse placeOrder(Integer userId) {
        Cart cart = cartRepository.findByUserUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Giỏ hàng trống"));

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng không có sản phẩm nào");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user"));

        for (CartItem item : cart.getCartItems()) {
            Product product = item.getProduct();
            if (product.getStock() < item.getQuantity()) {
                throw new IllegalStateException(
                        "Sản phẩm '" + product.getProductName() + "' không đủ số lượng trong kho");
            }
        }

        Order order = Order.builder()
                .user(user)
                .status("Pending")
                .build();
        order = orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cart.getCartItems()) {
            Product product = item.getProduct();
            BigDecimal price = product.getPrice();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .price(price)
                    .build();
            orderDetailRepository.save(detail);

            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }

        order.setTotalAmount(total);
        order = orderRepository.save(order);

        cartService.clearCart(userId);

        // FIX: load lại bằng fetch join để tránh Lazy lỗi
        return toResponse(orderRepository.findByIdWithDetails(order.getOrderId()).get());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(Integer orderId) {
        return toResponse(findOrThrow(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getByUser(Integer userId) {
        return orderRepository.findByUserWithDetails(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAll() {
        // FIX QUAN TRỌNG: dùng fetch join
        return orderRepository.findAllWithDetails().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Integer orderId, String status) {
        List<String> validStatuses = List.of("Pending", "Confirmed", "Shipping", "Completed", "Cancelled");
        if (!validStatuses.contains(status)) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + status);
        }

        Order order = findOrThrow(orderId);

        if ("Cancelled".equals(status) && !"Cancelled".equals(order.getStatus())) {
            for (OrderDetail detail : order.getOrderDetails()) {
                Product product = detail.getProduct();
                product.setStock(product.getStock() + detail.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(status);
        order = orderRepository.save(order);

        // FIX: load lại tránh Lazy lỗi
        return toResponse(orderRepository.findByIdWithDetails(order.getOrderId()).get());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Order findOrThrow(Integer id) {
        return orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng id: " + id));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderDetailResponse> details =
                order.getOrderDetails() == null ? List.of() :
                        order.getOrderDetails().stream().map(d -> {
                            BigDecimal subtotal = d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity()));
                            return OrderResponse.OrderDetailResponse.builder()
                                    .orderDetailId(d.getOrderDetailId())
                                    .productId(d.getProduct().getProductId())
                                    .productName(d.getProduct().getProductName())
                                    .imageUrl(d.getProduct().getImageUrl())
                                    .quantity(d.getQuantity())
                                    .price(d.getPrice())
                                    .subtotal(subtotal)
                                    .build();
                        }).collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUser().getUserId())
                .userFullName(order.getUser().getFullName())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .details(details)
                .build();
    }
}