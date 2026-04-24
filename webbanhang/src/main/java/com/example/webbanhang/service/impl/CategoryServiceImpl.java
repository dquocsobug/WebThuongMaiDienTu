package com.example.webbanhang.service.impl;

import com.example.webbanhang.dto.request.CategoryRequest;
import com.example.webbanhang.entity.Category;
import com.example.webbanhang.repository.CategoryRepository;
import com.example.webbanhang.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Category create(CategoryRequest request) {
        Category category = Category.builder()
                .categoryName(request.getCategoryName())
                .description(request.getDescription())
                .build();
        return categoryRepository.save(category);
    }

    @Override
    public Category update(Integer id, CategoryRequest request) {
        Category category = findOrThrow(id);
        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());
        return categoryRepository.save(category);
    }

    @Override
    public void delete(Integer id) {
        findOrThrow(id);
        categoryRepository.deleteById(id);
    }

    @Override
    public Category getById(Integer id) {
        return findOrThrow(id);
    }

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    private Category findOrThrow(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy danh mục id: " + id));
    }
}