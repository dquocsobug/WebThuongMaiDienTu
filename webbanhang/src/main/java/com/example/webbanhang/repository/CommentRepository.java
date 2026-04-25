package com.example.webbanhang.repository;

import com.example.webbanhang.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    // ── Lấy comment của bài viết (phân trang) ────────────────────────────────

    Page<Comment> findByPostPostIdOrderByCreatedAtAsc(Integer postId, Pageable pageable);

    List<Comment> findByPostPostIdOrderByCreatedAtAsc(Integer postId);

    // ── Lấy comment của user ──────────────────────────────────────────────────

    Page<Comment> findByUserUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    // ── Đếm số comment của bài viết ──────────────────────────────────────────

    long countByPostPostId(Integer postId);

    // ── Kiểm tra comment thuộc sở hữu của user ───────────────────────────────

    boolean existsByCommentIdAndUserUserId(Integer commentId, Integer userId);

    // ── Xoá toàn bộ comment của bài viết (khi xoá bài viết) ──────────────────

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.post.postId = :postId")
    void deleteByPostPostId(@Param("postId") Integer postId);

    // ── Xoá toàn bộ comment của user (khi admin xoá user) ────────────────────

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.user.userId = :userId")
    void deleteByUserUserId(@Param("userId") Integer userId);
}
