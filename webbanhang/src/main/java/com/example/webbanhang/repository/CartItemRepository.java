package com.example.webbanhang.repository;

import com.example.webbanhang.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    Optional<CartItem> findByCartCartIdAndProductProductId(Integer cartId, Integer productId);
}