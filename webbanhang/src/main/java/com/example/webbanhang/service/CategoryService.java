package com.example.webbanhang.service;

import com.example.webbanhang.dto.request.CategoryRequest;
import com.example.webbanhang.entity.Category;
import java.util.List;

public interface CategoryService {
    Category create(CategoryRequest request);
    Category update(Integer id, CategoryRequest request);
    void delete(Integer id);
    Category getById(Integer id);
    List<Category> getAll();
}