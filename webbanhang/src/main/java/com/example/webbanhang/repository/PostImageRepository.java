package com.example.webbanhang.repository;

import com.example.webbanhang.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Integer> {

    // ── Lấy tất cả ảnh của bài viết ──────────────────────────────────────────

    List<PostImage> findByPostPostIdOrderByDisplayOrderAsc(Integer postId);

    // ── Lấy ảnh chính của bài viết ────────────────────────────────────────────

    Optional<PostImage> findByPostPostIdAndIsMainTrue(Integer postId);

    // ── Đếm số ảnh của bài viết ───────────────────────────────────────────────

    long countByPostPostId(Integer postId);

    // ── Bỏ cờ IsMain toàn bộ ảnh cũ của bài viết ─────────────────────────────

    @Modifying
    @Query("UPDATE PostImage pi SET pi.isMain = false WHERE pi.post.postId = :postId")
    void clearMainImageByPostId(@Param("postId") Integer postId);

    // ── Xoá toàn bộ ảnh của bài viết ─────────────────────────────────────────

    @Modifying
    @Query("DELETE FROM PostImage pi WHERE pi.post.postId = :postId")
    void deleteByPostPostId(@Param("postId") Integer postId);
}
