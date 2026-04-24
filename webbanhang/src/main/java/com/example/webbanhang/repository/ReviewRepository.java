package com.example.webbanhang.repository;

import com.example.webbanhang.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // ✅ FIX QUAN TRỌNG: fetch luôn user + product để tránh LazyInitializationException
    @Query("""
        SELECT r
        FROM Review r
        JOIN FETCH r.user u
        JOIN FETCH r.product p
        WHERE p.productId = :productId
        ORDER BY r.createdAt DESC
    """)
    List<Review> findByProductId(@Param("productId") Integer productId);

    // giữ nguyên (không cần fetch vì chỉ check exist)
    List<Review> findByUserUserId(Integer userId);

    boolean existsByUserUserIdAndProductProductId(Integer userId, Integer productId);

    // tính rating trung bình (OK)
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.productId = :productId")
    Double avgRatingByProductId(@Param("productId") Integer productId);
}