package com.example.webbanhang.repository;

import com.example.webbanhang.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("""
        SELECT DISTINCT o FROM Order o
        JOIN FETCH o.user
        LEFT JOIN FETCH o.orderDetails od
        LEFT JOIN FETCH od.product
        WHERE o.user.userId = :userId
        ORDER BY o.createdAt DESC
    """)
    List<Order> findByUserWithDetails(@Param("userId") Integer userId);

    @Query("""
        SELECT DISTINCT o FROM Order o
        JOIN FETCH o.user
        LEFT JOIN FETCH o.orderDetails od
        LEFT JOIN FETCH od.product
        WHERE o.orderId = :id
    """)
    Optional<Order> findByIdWithDetails(@Param("id") Integer id);

    @Query("""
        SELECT DISTINCT o FROM Order o
        JOIN FETCH o.user
        LEFT JOIN FETCH o.orderDetails od
        LEFT JOIN FETCH od.product
        ORDER BY o.createdAt DESC
    """)
    List<Order> findAllWithDetails();
}