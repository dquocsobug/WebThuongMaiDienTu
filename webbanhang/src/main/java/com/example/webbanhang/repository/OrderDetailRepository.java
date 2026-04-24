package com.example.webbanhang.repository;

import com.example.webbanhang.entity.Order;
import com.example.webbanhang.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    @Query("""
SELECT o FROM Order o
JOIN FETCH o.user
LEFT JOIN FETCH o.orderDetails od
LEFT JOIN FETCH od.product
WHERE o.id = :id
""")
    Optional<Order> findByIdWithDetails(Integer id);
}