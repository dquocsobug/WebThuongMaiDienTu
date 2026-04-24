package com.example.webbanhang.repository;

import com.example.webbanhang.entity.ProductPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ProductPromotionRepository extends JpaRepository<ProductPromotion, Integer> {

    List<ProductPromotion> findByProductProductId(Integer productId);

    List<ProductPromotion> findByPromotionPromotionId(Integer promotionId);

    boolean existsByProductProductIdAndPromotionPromotionId(Integer productId, Integer promotionId);

    void deleteByProductProductIdAndPromotionPromotionId(Integer productId, Integer promotionId);

    // JOIN FETCH promotion để load ngay trong cùng query
    // Tránh LazyInitializationException khi truy cập pp.getPromotion() ngoài session
    @Query("""
        SELECT pp FROM ProductPromotion pp
        JOIN FETCH pp.promotion promo
        WHERE pp.product.productId = :productId
          AND promo.startDate <= :now
          AND promo.endDate >= :now
    """)
    List<ProductPromotion> findActiveByProductId(
            @Param("productId") Integer productId,
            @Param("now") LocalDateTime now
    );
}