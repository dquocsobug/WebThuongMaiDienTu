package com.example.webbanhang.repository;

import com.example.webbanhang.entity.User;
import com.example.webbanhang.enums.Role;
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
public interface UserRepository extends JpaRepository<User, Integer> {

    // ── Tìm theo email ────────────────────────────────────────────────────────

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // ── Tìm theo role ─────────────────────────────────────────────────────────

    List<User> findByRole(Role role);

    Page<User> findByRoleOrderByCreatedAtDesc(Role role, Pageable pageable);

    // ── Tìm kiếm user (Admin) ─────────────────────────────────────────────────

    @Query("""
        SELECT u FROM User u
        WHERE (:keyword IS NULL
                OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR u.phone           LIKE CONCAT('%', :keyword, '%'))
          AND (:role IS NULL OR u.role = :role)
        ORDER BY u.createdAt DESC
        """)
    Page<User> findWithFilters(
            @Param("keyword") String keyword,
            @Param("role")    Role role,
            Pageable pageable);

    // ── Nâng cấp role ─────────────────────────────────────────────────────────

    @Modifying
    @Query("UPDATE User u SET u.role = :role WHERE u.userId = :userId")
    void updateRole(
            @Param("userId") Integer userId,
            @Param("role")   Role role);

    // ── Tìm USER đủ điều kiện nâng cấp LOYAL_CUSTOMER ────────────────────────
    // Điều kiện: đã có đơn DELIVERED tạo trước ngày cutoff (1 tháng trước)

    @Query("""
        SELECT DISTINCT u FROM User u
        JOIN u.orders o
        WHERE u.role = 'USER'
          AND o.status = 'DELIVERED'
          AND o.createdAt <= :cutoff
        """)
    List<User> findEligibleForLoyalUpgrade(@Param("cutoff") LocalDateTime cutoff);

    // ── Đếm theo role (Dashboard Admin) ──────────────────────────────────────

    long countByRole(Role role);
}
