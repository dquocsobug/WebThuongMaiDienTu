package com.example.webbanhang.service;

import com.example.webbanhang.dto.request.ReviewRequest;
import com.example.webbanhang.dto.response.ReviewResponse;
import java.util.List;

public interface ReviewService {
    ReviewResponse create(Integer userId, ReviewRequest request);
    ReviewResponse update(Integer userId, Integer reviewId, ReviewRequest request);
    void delete(Integer userId, Integer reviewId);
    List<ReviewResponse> getByProduct(Integer productId);
    List<ReviewResponse> getByUser(Integer userId);
}