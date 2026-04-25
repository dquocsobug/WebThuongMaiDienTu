package com.example.webbanhang.repository;

import com.example.webbanhang.entity.ProductPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductPromotionRepository extends JpaRepository<ProductPromotion, Integer> {

    // ── Kiểm tra đã gán chưa ─────────────────────────────────────────────────

    boolean existsByProductProductIdAndPromotionPromotionId(
            Integer productId, Integer promotionId);

    // ── Lấy bản ghi theo productId + promotionId ─────────────────────────────

    Optional<ProductPromotion> findByProductProductIdAndPromotionPromotionId(
            Integer productId, Integer promotionId);

    // ── Lấy tất cả khuyến mãi của sản phẩm ──────────────────────────────────

    List<ProductPromotion> findByProductProductId(Integer productId);

    // ── Lấy tất cả sản phẩm trong khuyến mãi ────────────────────────────────

    List<ProductPromotion> findByPromotionPromotionId(Integer promotionId);

    // ── Xoá theo productId + promotionId ─────────────────────────────────────

    @Modifying
    @Query("""
        DELETE FROM ProductPromotion pp
        WHERE pp.product.productId = :productId
          AND pp.promotion.promotionId = :promotionId
        """)
    void deleteByProductIdAndPromotionId(
            @Param("productId")   Integer productId,
            @Param("promotionId") Integer promotionId);

    // ── Xoá toàn bộ gán của một sản phẩm ────────────────────────────────────

    @Modifying
    @Query("DELETE FROM ProductPromotion pp WHERE pp.product.productId = :productId")
    void deleteAllByProductId(@Param("productId") Integer productId);

    // ── Xoá toàn bộ gán của một khuyến mãi ───────────────────────────────────

    @Modifying
    @Query("DELETE FROM ProductPromotion pp WHERE pp.promotion.promotionId = :promotionId")
    void deleteAllByPromotionId(@Param("promotionId") Integer promotionId);
}
