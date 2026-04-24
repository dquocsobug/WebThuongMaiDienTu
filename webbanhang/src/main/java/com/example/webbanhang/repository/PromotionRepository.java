package com.example.webbanhang.repository;

import com.example.webbanhang.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    @Query("SELECT p FROM Promotion p WHERE p.startDate <= :now AND p.endDate >= :now")
    List<Promotion> findActivePromotions(LocalDateTime now);
}