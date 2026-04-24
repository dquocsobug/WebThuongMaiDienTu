package com.example.webbanhang.service;

import com.example.webbanhang.dto.request.PostRequest;
import com.example.webbanhang.dto.response.PostResponse;
import java.util.List;

public interface PostService {
    PostResponse create(Integer userId, PostRequest request);
    PostResponse update(Integer userId, Integer postId, PostRequest request);
    void delete(Integer userId, Integer postId);
    PostResponse getById(Integer id);
    List<PostResponse> getAll();
    List<PostResponse> search(String keyword);
}