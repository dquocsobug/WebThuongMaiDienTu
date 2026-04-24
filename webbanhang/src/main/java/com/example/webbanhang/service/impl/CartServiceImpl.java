package com.example.webbanhang.service.impl;

import com.example.webbanhang.dto.request.CartItemRequest;
import com.example.webbanhang.dto.response.CartResponse;
import com.example.webbanhang.entity.*;
import com.example.webbanhang.repository.*;
import com.example.webbanhang.service.CartService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public CartResponse getCart(Integer userId) {
        Cart cart = getOrCreateCart(userId);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(Integer userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm id: " + request.getProductId()));

        if (product.getStock() < request.getQuantity()) {
            throw new IllegalArgumentException("Sản phẩm không đủ số lượng trong kho");
        }

        // Nếu sản phẩm đã có trong giỏ → cộng thêm số lượng
        cartItemRepository.findByCartCartIdAndProductProductId(cart.getCartId(), product.getProductId())
                .ifPresentOrElse(
                        item -> {
                            int newQty = item.getQuantity() + request.getQuantity();
                            if (product.getStock() < newQty) {
                                throw new IllegalArgumentException("Vượt quá số lượng tồn kho");
                            }
                            item.setQuantity(newQty);
                            cartItemRepository.save(item);
                        },
                        () -> {
                            CartItem newItem = CartItem.builder()
                                    .cart(cart)
                                    .product(product)
                                    .quantity(request.getQuantity())
                                    .build();
                            cartItemRepository.save(newItem);
                        }
                );

        // Reload cart để lấy dữ liệu mới nhất
        return toResponse(cartRepository.findById(cart.getCartId()).orElseThrow());
    }

    @Override
    @Transactional
    public CartResponse updateItem(Integer userId, Integer cartItemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy cart item id: " + cartItemId));

        // Kiểm tra item thuộc cart của user
        if (!item.getCart().getUser().getUserId().equals(userId)) {
            throw new SecurityException("Bạn không có quyền sửa item này");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            if (item.getProduct().getStock() < quantity) {
                throw new IllegalArgumentException("Vượt quá số lượng tồn kho");
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        Cart cart = cartRepository.findByUserUserId(userId).orElseThrow();
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Integer userId, Integer cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy cart item id: " + cartItemId));

        if (!item.getCart().getUser().getUserId().equals(userId)) {
            throw new SecurityException("Bạn không có quyền xóa item này");
        }

        cartItemRepository.delete(item);

        Cart cart = cartRepository.findByUserUserId(userId).orElseThrow();
        return toResponse(cart);
    }

    @Override
    @Transactional
    public void clearCart(Integer userId) {
        cartRepository.findByUserUserId(userId).ifPresent(cart -> {
            cart.getCartItems().clear();
            cartRepository.save(cart);
        });
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Cart getOrCreateCart(Integer userId) {
        return cartRepository.findByUserUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user id: " + userId));
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse toResponse(Cart cart) {
        List<CartResponse.CartItemResponse> items = cart.getCartItems() == null
                ? List.of()
                : cart.getCartItems().stream().map(item -> {
            BigDecimal subtotal = item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            return CartResponse.CartItemResponse.builder()
                    .cartItemId(item.getCartItemId())
                    .productId(item.getProduct().getProductId())
                    .productName(item.getProduct().getProductName())
                    .imageUrl(item.getProduct().getImageUrl())
                    .price(item.getProduct().getPrice())
                    .quantity(item.getQuantity())
                    .subtotal(subtotal)
                    .build();
        }).collect(Collectors.toList());

        BigDecimal total = items.stream()
                .map(CartResponse.CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .userId(cart.getUser().getUserId())
                .items(items)
                .totalPrice(total)
                .build();
    }
}