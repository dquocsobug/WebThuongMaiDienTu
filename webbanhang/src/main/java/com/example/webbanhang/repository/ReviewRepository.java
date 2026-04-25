package com.example.webbanhang.repository;

import com.example.webbanhang.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // ── Lấy tất cả review của sản phẩm ───────────────────────────────────────

    Page<Review> findByProductProductIdOrderByCreatedAtDesc(Integer productId, Pageable pageable);

    List<Review> findByProductProductId(Integer productId);

    // ── Lấy review của user ───────────────────────────────────────────────────

    List<Review> findByUserUserIdOrderByCreatedAtDesc(Integer userId);

    // ── Lấy review theo userId + productId (unique) ───────────────────────────

    Optional<Review> findByUserUserIdAndProductProductId(Integer userId, Integer productId);

    // ── Kiểm tra user đã review sản phẩm chưa ────────────────────────────────

    boolean existsByUserUserIdAndProductProductId(Integer userId, Integer productId);

    // ── Tính điểm trung bình của sản phẩm ────────────────────────────────────

    @Query("""
        SELECT COALESCE(AVG(CAST(r.rating AS double)), 0.0)
        FROM Review r
        WHERE r.product.productId = :productId
        """)
    Double calculateAverageRating(@Param("productId") Integer productId);

    // ── Đếm số review theo rating ─────────────────────────────────────────────

    @Query("""
        SELECT r.rating, COUNT(r)
        FROM Review r
        WHERE r.product.productId = :productId
        GROUP BY r.rating
        ORDER BY r.rating DESC
        """)
    List<Object[]> countByRatingGrouped(@Param("productId") Integer productId);

    // ── Đếm tổng review của sản phẩm ─────────────────────────────────────────

    long countByProductProductId(Integer productId);
}
