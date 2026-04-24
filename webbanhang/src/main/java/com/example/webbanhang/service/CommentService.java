package com.example.webbanhang.service;

import com.example.webbanhang.dto.request.CommentRequest;
import com.example.webbanhang.dto.response.CommentResponse;
import java.util.List;

public interface CommentService {
    CommentResponse create(Integer userId, CommentRequest request);
    CommentResponse update(Integer userId, Integer commentId, String content);
    void delete(Integer userId, Integer commentId);
    List<CommentResponse> getByProduct(Integer productId);
}