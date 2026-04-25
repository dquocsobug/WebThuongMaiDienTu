package com.example.webbanhang.repository;

import com.example.webbanhang.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    // ── Lấy tất cả chi tiết của một đơn hàng ─────────────────────────────────

    List<OrderDetail> findByOrderOrderId(Integer orderId);

    // ── Lấy chi tiết kèm Product (tránh N+1) ─────────────────────────────────

    @Query("""
        SELECT od FROM OrderDetail od
        LEFT JOIN FETCH od.product p
        LEFT JOIN FETCH p.images img
        WHERE od.order.orderId = :orderId
        """)
    List<OrderDetail> findByOrderIdWithProduct(@Param("orderId") Integer orderId);

    // ── Kiểm tra user đã mua sản phẩm và đơn đã DELIVERED chưa ──────────────
    // Dùng để validate quyền viết Review

    @Query("""
        SELECT COUNT(od) > 0 FROM OrderDetail od
        WHERE od.product.productId = :productId
          AND od.order.user.userId = :userId
          AND od.order.status = 'DELIVERED'
        """)
    boolean hasPurchasedAndDelivered(
            @Param("userId")    Integer userId,
            @Param("productId") Integer productId);

    // ── Thống kê sản phẩm bán chạy ───────────────────────────────────────────

    @Query("""
        SELECT od.product.productId, od.product.productName, SUM(od.quantity) AS totalSold
        FROM OrderDetail od
        WHERE od.order.status = 'DELIVERED'
        GROUP BY od.product.productId, od.product.productName
        ORDER BY totalSold DESC
        """)
    List<Object[]> findTopSellingProducts(org.springframework.data.domain.Pageable pageable);
}
