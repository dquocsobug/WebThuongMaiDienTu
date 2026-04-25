package com.example.webbanhang.repository;

import com.example.webbanhang.entity.PostProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostProductRepository extends JpaRepository<PostProduct, Integer> {

    // ── Kiểm tra đã gán chưa ─────────────────────────────────────────────────

    boolean existsByPostPostIdAndProductProductId(Integer postId, Integer productId);

    // ── Lấy bản ghi theo postId + productId ──────────────────────────────────

    Optional<PostProduct> findByPostPostIdAndProductProductId(Integer postId, Integer productId);

    // ── Lấy tất cả sản phẩm trong bài viết (sắp theo thứ tự hiển thị) ────────

    List<PostProduct> findByPostPostIdOrderByDisplayOrderAsc(Integer postId);

    // ── Lấy tất cả bài viết có chứa sản phẩm ────────────────────────────────

    @Query("""
        SELECT pp FROM PostProduct pp
        WHERE pp.product.productId = :productId
          AND pp.post.status = 'PUBLISHED'
        ORDER BY pp.post.createdAt DESC
        """)
    List<PostProduct> findPublishedPostsByProductId(@Param("productId") Integer productId);

    // ── Đếm số sản phẩm trong bài viết ───────────────────────────────────────

    long countByPostPostId(Integer postId);

    // ── Xoá liên kết theo postId + productId ─────────────────────────────────

    @Modifying
    @Query("""
        DELETE FROM PostProduct pp
        WHERE pp.post.postId = :postId
          AND pp.product.productId = :productId
        """)
    void deleteByPostIdAndProductId(
            @Param("postId")    Integer postId,
            @Param("productId") Integer productId);

    // ── Xoá toàn bộ sản phẩm của bài viết ────────────────────────────────────

    @Modifying
    @Query("DELETE FROM PostProduct pp WHERE pp.post.postId = :postId")
    void deleteAllByPostId(@Param("postId") Integer postId);

    // ── Xoá toàn bộ liên kết của sản phẩm (khi xoá sản phẩm) ────────────────

    @Modifying
    @Query("DELETE FROM PostProduct pp WHERE pp.product.productId = :productId")
    void deleteAllByProductId(@Param("productId") Integer productId);
}
