package com.example.webbanhang.repository;

import com.example.webbanhang.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    // ── Lấy item theo cartId + productId ─────────────────────────────────────

    Optional<CartItem> findByCartCartIdAndProductProductId(Integer cartId, Integer productId);

    // ── Lấy toàn bộ item trong cart ──────────────────────────────────────────

    List<CartItem> findByCartCartId(Integer cartId);

    // ── Kiểm tra sản phẩm đã có trong cart chưa ─────────────────────────────

    boolean existsByCartCartIdAndProductProductId(Integer cartId, Integer productId);

    // ── Đếm số loại sản phẩm trong cart ─────────────────────────────────────

    long countByCartCartId(Integer cartId);

    // ── Xoá item theo cartId + productId ─────────────────────────────────────

    @Modifying
    @Query("""
        DELETE FROM CartItem ci
        WHERE ci.cart.cartId = :cartId
          AND ci.product.productId = :productId
        """)
    void deleteByCartIdAndProductId(
            @Param("cartId")    Integer cartId,
            @Param("productId") Integer productId);

    // ── Xoá toàn bộ item trong cart (sau khi đặt hàng) ───────────────────────

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    void deleteAllByCartId(@Param("cartId") Integer cartId);
}
