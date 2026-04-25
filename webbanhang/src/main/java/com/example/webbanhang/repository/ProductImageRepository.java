package com.example.webbanhang.repository;

import com.example.webbanhang.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {

    // ── Lấy tất cả ảnh của sản phẩm ─────────────────────────────────────────

    List<ProductImage> findByProductProductIdOrderByDisplayOrderAsc(Integer productId);

    // ── Lấy ảnh chính của sản phẩm ──────────────────────────────────────────

    Optional<ProductImage> findByProductProductIdAndIsMainTrue(Integer productId);

    // ── Kiểm tra sản phẩm có ảnh nào không ──────────────────────────────────

    boolean existsByProductProductId(Integer productId);

    // ── Đếm số ảnh của sản phẩm ─────────────────────────────────────────────

    long countByProductProductId(Integer productId);

    // ── Bỏ cờ IsMain toàn bộ ảnh cũ trước khi đặt ảnh chính mới ────────────

    @Modifying
    @Query("UPDATE ProductImage pi SET pi.isMain = false WHERE pi.product.productId = :productId")
    void clearMainImageByProductId(@Param("productId") Integer productId);

    // ── Xoá toàn bộ ảnh của sản phẩm ────────────────────────────────────────

    @Modifying
    @Query("DELETE FROM ProductImage pi WHERE pi.product.productId = :productId")
    void deleteByProductProductId(@Param("productId") Integer productId);
}
