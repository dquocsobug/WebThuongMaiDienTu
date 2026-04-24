package com.example.webbanhang.repository;

import com.example.webbanhang.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    // Cách 1: dùng JOIN FETCH (khuyên dùng)
    @Query("""
        SELECT c 
        FROM Comment c
        JOIN FETCH c.user u
        JOIN FETCH c.product p
        WHERE p.productId = :productId
        ORDER BY c.createdAt DESC
    """)
    List<Comment> findByProductId(@Param("productId") Integer productId);
}