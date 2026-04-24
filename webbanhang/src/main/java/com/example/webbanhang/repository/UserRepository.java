package com.example.webbanhang.repository;

import com.example.webbanhang.entity.User;
import com.example.webbanhang.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    /**
     * Tìm các USER thường đủ điều kiện trở thành LOYAL_CUSTOMER:
     * đã có ít nhất 1 đơn DELIVERED trước ngày cutoff.
     */
    @Query("""
        SELECT DISTINCT u FROM User u
        JOIN u.orders o
        WHERE u.role = 'USER'
          AND o.status = 'DELIVERED'
          AND o.createdAt <= :cutoff
        """)
    List<User> findEligibleForLoyalUpgrade(@Param("cutoff") LocalDateTime cutoff);
}
