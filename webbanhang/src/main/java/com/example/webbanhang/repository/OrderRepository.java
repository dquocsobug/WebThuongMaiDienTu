package com.example.webbanhang.repository;

import com.example.webbanhang.entity.Order;
import com.example.webbanhang.enums.OrderStatus;
import com.example.webbanhang.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // ── Lấy theo userId ──────────────────────────────────────────────────────

    Page<Order> findByUserUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    List<Order> findByUserUserIdOrderByCreatedAtDesc(Integer userId);

    // ── Lấy theo userId + trạng thái ─────────────────────────────────────────

    List<Order> findByUserUserIdAndStatus(Integer userId, OrderStatus status);

    // ── Lấy theo trạng thái (Admin quản lý) ──────────────────────────────────

    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    // ── Lấy theo trạng thái thanh toán ───────────────────────────────────────

    Page<Order> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);

    // ── Lấy order kèm OrderDetails + Product (tránh N+1) ─────────────────────

    @Query("""
        SELECT o FROM Order o
        LEFT JOIN FETCH o.orderDetails od
        LEFT JOIN FETCH od.product p
        WHERE o.orderId = :orderId
        """)
    Optional<Order> findByIdWithDetails(@Param("orderId") Integer orderId);

    // ── Kiểm tra user có đơn DELIVERED trước ngày cutoff không ───────────────
    // Dùng để xét điều kiện nâng cấp LOYAL_CUSTOMER

    @Query("""
        SELECT COUNT(o) > 0 FROM Order o
        WHERE o.user.userId = :userId
          AND o.status = 'DELIVERED'
          AND o.createdAt <= :cutoff
        """)
    boolean hasDeliveredOrderBefore(
            @Param("userId") Integer userId,
            @Param("cutoff") LocalDateTime cutoff);

    // ── Đếm tổng đơn theo trạng thái (Dashboard Admin) ───────────────────────

    long countByStatus(OrderStatus status);

    // ── Tổng doanh thu theo khoảng thời gian ─────────────────────────────────

    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o
        WHERE o.status = 'DELIVERED'
          AND o.createdAt BETWEEN :from AND :to
        """)
    BigDecimal sumRevenueByDateRange(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);

    // ── Đơn hàng theo khoảng thời gian ───────────────────────────────────────

    @Query("""
        SELECT o FROM Order o
        WHERE o.createdAt BETWEEN :from AND :to
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findByDateRange(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to,
            Pageable pageable);

    // ── Lọc đơn hàng kết hợp (Admin filter) ──────────────────────────────────

    @Query("""
        SELECT o FROM Order o
        WHERE (:userId   IS NULL OR o.user.userId = :userId)
          AND (:status   IS NULL OR o.status = :status)
          AND (:fromDate IS NULL OR o.createdAt >= :fromDate)
          AND (:toDate   IS NULL OR o.createdAt <= :toDate)
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findWithFilters(
            @Param("userId")   Integer userId,
            @Param("status")   OrderStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate")   LocalDateTime toDate,
            Pageable pageable);
}
