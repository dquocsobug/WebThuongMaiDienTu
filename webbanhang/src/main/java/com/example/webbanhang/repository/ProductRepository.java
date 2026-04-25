package com.example.webbanhang.repository;

import com.example.webbanhang.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // ── Tìm theo danh mục ───────────────────────────────────────────────────

    Page<Product> findByCategoryCategoryId(Integer categoryId, Pageable pageable);

    List<Product> findByCategoryCategoryId(Integer categoryId);

    // ── Tìm kiếm full-text ───────────────────────────────────────────────────

    @Query("""
        SELECT p FROM Product p
        WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // ── Lọc theo khoảng giá ─────────────────────────────────────────────────

    @Query("""
        SELECT p FROM Product p
        WHERE (:minPrice IS NULL OR p.price >= :minPrice)
          AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        """)
    Page<Product> findByPriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    // ── Lọc kết hợp: keyword + category + price ──────────────────────────────

    @Query("""
        SELECT p FROM Product p
        WHERE (:keyword   IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:categoryId IS NULL OR p.category.categoryId = :categoryId)
          AND (:minPrice   IS NULL OR p.price >= :minPrice)
          AND (:maxPrice   IS NULL OR p.price <= :maxPrice)
        """)
    Page<Product> findWithFilters(
            @Param("keyword")    String keyword,
            @Param("categoryId") Integer categoryId,
            @Param("minPrice")   BigDecimal minPrice,
            @Param("maxPrice")   BigDecimal maxPrice,
            Pageable pageable);

    // ── Tồn kho ─────────────────────────────────────────────────────────────

    List<Product> findByStockLessThan(int threshold);

    List<Product> findByStockGreaterThan(int threshold);

    // ── Cập nhật tồn kho ─────────────────────────────────────────────────────

    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :qty WHERE p.productId = :id AND p.stock >= :qty")
    int decreaseStock(@Param("id") Integer productId, @Param("qty") int quantity);

    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock + :qty WHERE p.productId = :id")
    int increaseStock(@Param("id") Integer productId, @Param("qty") int quantity);

    // ── Sản phẩm nổi bật (có trong bài viết đã publish) ────────────────────

    @Query("""
        SELECT DISTINCT pp.product FROM PostProduct pp
        WHERE pp.post.status = 'PUBLISHED'
        ORDER BY pp.displayOrder ASC
        """)
    List<Product> findFeaturedProducts(Pageable pageable);

    // ── Sản phẩm có khuyến mãi đang hoạt động ──────────────────────────────

    @Query("""
        SELECT DISTINCT pp.product FROM ProductPromotion pp
        WHERE pp.promotion.startDate <= CURRENT_TIMESTAMP
          AND pp.promotion.endDate   >= CURRENT_TIMESTAMP
        """)
    List<Product> findProductsWithActivePromotion();

    // ── Kiểm tra sản phẩm thuộc danh mục ────────────────────────────────────

    boolean existsByCategoryCategoryId(Integer categoryId);
}
