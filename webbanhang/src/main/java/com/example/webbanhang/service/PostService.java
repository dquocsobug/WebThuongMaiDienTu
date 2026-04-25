package com.example.webbanhang.service;

import com.example.webbanhang.dto.request.PostRequest;
import com.example.webbanhang.dto.request.ReviewPostRequest;
import com.example.webbanhang.dto.response.PageResponse;
import com.example.webbanhang.dto.response.PostResponse;
import com.example.webbanhang.dto.response.PostSummaryResponse;
import com.example.webbanhang.enums.PostStatus;
import org.springframework.data.domain.Pageable;

public interface PostService {

    // ── Public (khách vãng lai) ───────────────────────────────────────────────

    /** Lấy danh sách bài viết đã publish. */
    PageResponse<PostSummaryResponse> getPublishedPosts(String keyword, Pageable pageable);

    /** Lấy chi tiết bài viết đã publish. */
    PostResponse getPublishedPost(Integer postId);

    // ── Writer / Loyal Customer ───────────────────────────────────────────────

    /** Lấy danh sách bài viết của tác giả đang đăng nhập. */
    PageResponse<PostSummaryResponse> getMyPosts(Integer userId, PostStatus status, Pageable pageable);

    /** Lấy chi tiết bài viết — tác giả có thể xem mọi trạng thái của mình. */
    PostResponse getMyPost(Integer userId, Integer postId);

    /** Tạo bài viết mới (status = DRAFT). */
    PostResponse create(Integer userId, PostRequest request);

    /** Cập nhật bài viết — chỉ được khi status là DRAFT hoặc REJECTED. */
    PostResponse update(Integer userId, Integer postId, PostRequest request);

    /** Gửi bài viết lên Admin duyệt (status DRAFT/REJECTED → PENDING). */
    PostResponse submit(Integer userId, Integer postId);

    /** Xóa bài viết — chỉ được khi DRAFT hoặc REJECTED. */
    void delete(Integer userId, Integer postId);

    // ── Admin ─────────────────────────────────────────────────────────────────

    /** Lấy tất cả bài viết theo filter (status, authorId, keyword). */
    PageResponse<PostSummaryResponse> adminGetAll(PostStatus status,
                                                  Integer authorId,
                                                  String keyword,
                                                  Pageable pageable);

    /** Duyệt hoặc từ chối bài viết. */
    PostResponse reviewPost(Integer postId, ReviewPostRequest request);

    /** Admin xóa bất kỳ bài viết nào. */
    void adminDelete(Integer postId);
}