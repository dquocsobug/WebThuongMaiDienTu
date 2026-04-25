package com.example.webbanhang.repository;

import com.example.webbanhang.entity.Post;
import com.example.webbanhang.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    // ── Lấy bài viết theo trạng thái (public feed) ───────────────────────────

    Page<Post> findByStatusOrderByCreatedAtDesc(PostStatus status, Pageable pageable);

    // ── Lấy bài viết của một tác giả ─────────────────────────────────────────

    Page<Post> findByCreatedByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    // ── Lấy bài viết của tác giả theo trạng thái ─────────────────────────────

    Page<Post> findByCreatedByUserIdAndStatusOrderByCreatedAtDesc(
            Integer userId, PostStatus status, Pageable pageable);

    // ── Tìm kiếm bài viết đã publish ─────────────────────────────────────────

    @Query("""
        SELECT p FROM Post p
        WHERE p.status = 'PUBLISHED'
          AND (LOWER(p.title)   LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.summary) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY p.createdAt DESC
        """)
    Page<Post> searchPublished(@Param("keyword") String keyword, Pageable pageable);

    // ── Admin lọc kết hợp ────────────────────────────────────────────────────

    @Query("""
        SELECT p FROM Post p
        WHERE (:status  IS NULL OR p.status = :status)
          AND (:authorId IS NULL OR p.createdBy.userId = :authorId)
          AND (:keyword  IS NULL
                OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY p.createdAt DESC
        """)
    Page<Post> findWithFilters(
            @Param("status")   PostStatus status,
            @Param("authorId") Integer authorId,
            @Param("keyword")  String keyword,
            Pageable pageable);

    // ── Lấy post kèm images + postProducts + product (tránh N+1) ─────────────

    @Query("""
        SELECT DISTINCT p FROM Post p
        LEFT JOIN FETCH p.images
        WHERE p.postId = :postId
        """)
    Optional<Post> findByIdWithImages(@Param("postId") Integer postId);

    @Query("""
        SELECT DISTINCT p FROM Post p
        LEFT JOIN FETCH p.postProducts pp
        LEFT JOIN FETCH pp.product prod
        WHERE p.postId = :postId
        """)
    Optional<Post> findByIdWithProducts(@Param("postId") Integer postId);

    // ── Đếm bài viết theo trạng thái (Dashboard Admin) ───────────────────────

    long countByStatus(PostStatus status);

    // ── Đếm bài viết của tác giả ─────────────────────────────────────────────

    long countByCreatedByUserId(Integer userId);
}
