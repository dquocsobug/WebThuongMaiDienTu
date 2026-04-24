package com.example.webbanhang.repository;

import com.example.webbanhang.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Integer> {

    @Query("""
        SELECT p FROM Post p
        JOIN FETCH p.createdBy
        ORDER BY p.createdAt DESC
    """)
    List<Post> findAllWithUser();

    @Query("""
        SELECT p FROM Post p
        JOIN FETCH p.createdBy
        WHERE p.postId = :id
    """)
    Optional<Post> findByIdWithUser(@Param("id") Integer id);

    @Query("""
        SELECT p FROM Post p
        JOIN FETCH p.createdBy
        WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY p.createdAt DESC
    """)
    List<Post> searchByKeywordWithUser(@Param("keyword") String keyword);

    List<Post> findByCreatedByUserId(Integer userId);
}