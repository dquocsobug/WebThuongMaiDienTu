package com.example.webbanhang.service;

import com.example.webbanhang.dto.request.ProductRequest;
import com.example.webbanhang.dto.response.ProductResponse;
import java.util.List;

public interface ProductService {
    ProductResponse create(ProductRequest request);
    ProductResponse update(Integer id, ProductRequest request);
    void delete(Integer id);
    ProductResponse getById(Integer id);
    List<ProductResponse> getAll();
    List<ProductResponse> getByCategory(Integer categoryId);
    List<ProductResponse> search(String keyword);
}