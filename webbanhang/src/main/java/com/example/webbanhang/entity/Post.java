package com.example.webbanhang.entity;

import com.example.webbanhang.enums.PostStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PostID")
    private Integer postId;

    @Column(name = "Title", length = 255, nullable = false)
    private String title;

    @Column(name = "Content", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String content;

    @Column(name = "Summary", length = 500)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20, nullable = false)
    @Builder.Default
    private PostStatus status = PostStatus.DRAFT;

    @Column(name = "RejectionReason", length = 500)
    private String rejectionReason;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "PublishedAt")
    private LocalDateTime publishedAt;

    // ── Relationships ────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedBy", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PostImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PostProduct> postProducts = new ArrayList<>();
}
