package com.example.webbanhang.service.impl;

import com.example.webbanhang.dto.request.ProductRequest;
import com.example.webbanhang.dto.response.ProductResponse;
import com.example.webbanhang.entity.Category;
import com.example.webbanhang.entity.ProductPromotion;
import com.example.webbanhang.repository.CategoryRepository;
import com.example.webbanhang.repository.ProductPromotionRepository;
import com.example.webbanhang.repository.ProductRepository;
import com.example.webbanhang.repository.ReviewRepository;
import com.example.webbanhang.entity.Product;
import com.example.webbanhang.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)   // giữ session mở suốt method, tránh LazyInitializationException
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final ProductPromotionRepository productPromotionRepository;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy danh mục id: " + request.getCategoryId()));

        Product product = Product.builder()
                .productName(request.getProductName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock() != null ? request.getStock() : 0)
                .imageUrl(request.getImageUrl())
                .category(category)
                .build();

        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse update(Integer id, ProductRequest request) {
        Product product = findOrThrow(id);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy danh mục id: " + request.getCategoryId()));

        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock() != null ? request.getStock() : product.getStock());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);

        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        findOrThrow(id);
        productRepository.deleteById(id);
    }

    @Override
    public ProductResponse getById(Integer id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getByCategory(Integer categoryId) {
        return productRepository.findByCategoryCategoryId(categoryId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> search(String keyword) {
        return productRepository.searchByName(keyword).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Product findOrThrow(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm id: " + id));
    }

    public ProductResponse toResponse(Product p) {
        // JOIN FETCH trong query đã load Promotion sẵn — không cần lo LazyInit
        List<ProductPromotion> activePromos = productPromotionRepository
                .findActiveByProductId(p.getProductId(), LocalDateTime.now());

        // Lấy discount cao nhất nếu có nhiều khuyến mãi
        int maxDiscount = activePromos.stream()
                .mapToInt(pp -> pp.getPromotion().getDiscountPercent())
                .max()
                .orElse(0);

        BigDecimal discountedPrice = p.getPrice();
        if (maxDiscount > 0) {
            BigDecimal multiplier = BigDecimal.valueOf(100 - maxDiscount)
                    .divide(BigDecimal.valueOf(100));
            discountedPrice = p.getPrice().multiply(multiplier)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        Double avgRating = reviewRepository.avgRatingByProductId(p.getProductId());

        return ProductResponse.builder()
                .productId(p.getProductId())
                .productName(p.getProductName())
                .description(p.getDescription())
                .price(p.getPrice())
                .discountedPrice(maxDiscount > 0 ? discountedPrice : null)
                .discountPercent(maxDiscount > 0 ? maxDiscount : null)
                .stock(p.getStock())
                .imageUrl(p.getImageUrl())
                .categoryId(p.getCategory() != null ? p.getCategory().getCategoryId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getCategoryName() : null)
                .avgRating(avgRating)
                .createdAt(p.getCreatedAt())
                .build();
    }
}