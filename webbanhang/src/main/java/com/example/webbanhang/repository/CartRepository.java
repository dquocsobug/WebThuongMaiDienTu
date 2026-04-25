package com.example.webbanhang.repository;

import com.example.webbanhang.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    // ── Lấy giỏ hàng theo userId ─────────────────────────────────────────────

    Optional<Cart> findByUserUserId(Integer userId);

    // ── Kiểm tra user đã có cart chưa ────────────────────────────────────────

    boolean existsByUserUserId(Integer userId);

    // ── Lấy cart kèm CartItems + Product (tránh N+1) ─────────────────────────

    @Query("""
        SELECT c FROM Cart c
        LEFT JOIN FETCH c.cartItems ci
        LEFT JOIN FETCH ci.product p
        LEFT JOIN FETCH p.images img
        WHERE c.user.userId = :userId
        """)
    Optional<Cart> findByUserIdWithItems(@Param("userId") Integer userId);
}
