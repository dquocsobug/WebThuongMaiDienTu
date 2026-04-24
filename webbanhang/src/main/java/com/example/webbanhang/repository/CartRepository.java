package com.example.webbanhang.repository;

import com.example.webbanhang.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUserUserId(Integer userId);
}