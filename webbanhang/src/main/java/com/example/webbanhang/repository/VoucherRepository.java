package com.example.webbanhang.repository;

import com.example.webbanhang.entity.Voucher;
import com.example.webbanhang.enums.VoucherType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {

    // ── Tìm theo mã code ──────────────────────────────────────────────────────

    Optional<Voucher> findByCode(String code);

    boolean existsByCode(String code);

    // ── Tìm kiếm / lọc (Admin) ───────────────────────────────────────────────

    @Query("""
        SELECT v FROM Voucher v
        WHERE (:keyword      IS NULL OR LOWER(v.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                               OR LOWER(v.voucherName) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:voucherType  IS NULL OR v.voucherType = :voucherType)
          AND (:isActive     IS NULL OR v.isActive = :isActive)
          AND (:isLoyalOnly  IS NULL OR v.isLoyalOnly = :isLoyalOnly)
        ORDER BY v.createdAt DESC
        """)
    Page<Voucher> findWithFilters(
            @Param("keyword")     String keyword,
            @Param("voucherType") VoucherType voucherType,
            @Param("isActive")    Boolean isActive,
            @Param("isLoyalOnly") Boolean isLoyalOnly,
            Pageable pageable);

    // ── Lấy voucher còn hiệu lực ──────────────────────────────────────────────

    @Query("""
        SELECT v FROM Voucher v
        WHERE v.isActive = true
          AND (v.startDate IS NULL OR v.startDate <= :now)
          AND (v.endDate   IS NULL OR v.endDate   >= :now)
          AND (v.usageLimit IS NULL OR v.usedCount < v.usageLimit)
        ORDER BY v.endDate ASC
        """)
    List<Voucher> findValidVouchers(@Param("now") LocalDateTime now);

    // ── Lấy voucher dành riêng cho loyal customer ─────────────────────────────

    @Query("""
        SELECT v FROM Voucher v
        WHERE v.isActive = true
          AND v.isLoyalOnly = true
          AND (v.startDate IS NULL OR v.startDate <= :now)
          AND (v.endDate   IS NULL OR v.endDate   >= :now)
          AND (v.usageLimit IS NULL OR v.usedCount < v.usageLimit)
        """)
    List<Voucher> findLoyalOnlyVouchers(@Param("now") LocalDateTime now);

    // ── Lấy voucher sắp hết hạn ───────────────────────────────────────────────

    @Query("""
        SELECT v FROM Voucher v
        WHERE v.isActive = true
          AND v.endDate BETWEEN :now AND :threshold
        ORDER BY v.endDate ASC
        """)
    List<Voucher> findExpiringVouchers(
            @Param("now")       LocalDateTime now,
            @Param("threshold") LocalDateTime threshold);

    // ── Tăng usedCount sau khi áp dụng voucher ────────────────────────────────

    @Modifying
    @Query("UPDATE Voucher v SET v.usedCount = v.usedCount + 1 WHERE v.voucherId = :id")
    void incrementUsedCount(@Param("id") Integer voucherId);
}
