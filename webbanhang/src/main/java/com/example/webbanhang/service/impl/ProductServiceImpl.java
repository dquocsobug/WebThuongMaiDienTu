package com.example.webbanhang.service.impl;

import com.example.webbanhang.dto.request.ProductImageRequest;
import com.example.webbanhang.dto.request.ProductRequest;
import com.example.webbanhang.dto.response.*;
import com.example.webbanhang.entity.*;
import com.example.webbanhang.exception.BadRequestException;
import com.example.webbanhang.exception.ResourceNotFoundException;
import com.example.webbanhang.repository.*;
import com.example.webbanhang.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository        productRepository;
    private final ProductImageRepository   productImageRepository;
    private final CategoryRepository       categoryRepository;
    private final ReviewRepository         reviewRepository;
    private final PromotionRepository      promotionRepository;

    // ── Mapper ────────────────────────────────────────────────────────────────

    private ProductResponse toResponse(Product product) {

        // Ảnh
        List<ProductImage> images = productImageRepository
                .findByProductProductIdOrderByDisplayOrderAsc(product.getProductId());

        List<ProductImageResponse> imageResponses = images.stream()
                .map(img -> ProductImageResponse.builder()
                        .imageId(img.getImageId())
                        .imageUrl(img.getImageUrl())
                        .isMain(img.getIsMain())
                        .displayOrder(img.getDisplayOrder())
                        .build())
                .toList();

        String mainImageUrl = images.stream()
                .filter(ProductImage::getIsMain)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(images.isEmpty() ? null : images.get(0).getImageUrl());

        // Rating
        Double avgRating  = reviewRepository.calculateAverageRating(product.getProductId());
        long reviewCount   = reviewRepository.countByProductProductId(product.getProductId());

        // Khuyến mãi đang hoạt động — lấy % cao nhất
        List<Promotion> activePromotions = promotionRepository
                .findActivePromotionsByProductId(product.getProductId(), LocalDateTime.now());

        Integer discountPercent = activePromotions.stream()
                .map(Promotion::getDiscountPercent)
                .max(Integer::compareTo)
                .orElse(null);

        BigDecimal discountedPrice = null;
        if (discountPercent != null) {
            BigDecimal factor = BigDecimal.valueOf(100 - discountPercent).divide(BigDecimal.valueOf(100));
            discountedPrice = product.getPrice().multiply(factor)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }

        return ProductResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountedPrice(discountedPrice)
                .discountPercent(discountPercent)
                .stock(product.getStock())
                .categoryId(product.getCategory().getCategoryId())
                .categoryName(product.getCategory().getCategoryName())
                .images(imageResponses)
                .mainImageUrl(mainImageUrl)
                .averageRating(avgRating)
                .reviewCount(reviewCount)
                .createdAt(product.getCreatedAt())
                .build();
    }

    public ProductSummaryResponse toSummaryResponse(Product product) {
        String mainImageUrl = productImageRepository
                .findByProductProductIdAndIsMainTrue(product.getProductId())
                .map(ProductImage::getImageUrl)
                .orElse(null);
        return ProductSummaryResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .price(product.getPrice())
                .stock(product.getStock())
                .mainImageUrl(mainImageUrl)
                .categoryName(product.getCategory().getCategoryName())
                .build();
    }

    // ── Public ────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAll(String keyword,
                                                Integer categoryId,
                                                BigDecimal minPrice,
                                                BigDecimal maxPrice,
                                                Pageable pageable) {
        Page<Product> page = productRepository.findWithFilters(
                keyword, categoryId, minPrice, maxPrice, pageable);
        List<ProductResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.of(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        return toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductRatingStatsResponse getRatingStats(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", productId);
        }
        Double avg        = reviewRepository.calculateAverageRating(productId);
        long totalReviews = reviewRepository.countByProductProductId(productId);

        List<Object[]> rawDist = reviewRepository.countByRatingGrouped(productId);
        Map<Integer, Long> dist = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) dist.put(i, 0L);
        rawDist.forEach(row -> dist.put((Integer) row[0], (Long) row[1]));

        return ProductRatingStatsResponse.builder()
                .productId(productId)
                .averageRating(avg)
                .totalReviews(totalReviews)
                .ratingDistribution(dist)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getFeaturedProducts(Pageable pageable) {
        return productRepository.findFeaturedProducts(pageable).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsWithActivePromotion() {
        return productRepository.findProductsWithActivePromotion().stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        Product product = Product.builder()
                .productName(request.getProductName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(category)
                .build();

        productRepository.save(product);
        log.info("[Product] Tạo sản phẩm mới: id={}, name={}", product.getProductId(), product.getProductName());
        return toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse update(Integer productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(category);

        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void delete(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", productId);
        }
        productImageRepository.deleteByProductProductId(productId);
        productRepository.deleteById(productId);
        log.info("[Product] Xóa sản phẩm id={}", productId);
    }

    // ── Image management ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public ProductResponse addImage(Integer productId, ProductImageRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        // Nếu set isMain=true, bỏ cờ tất cả ảnh cũ trước
        if (Boolean.TRUE.equals(request.getIsMain())) {
            productImageRepository.clearMainImageByProductId(productId);
        }

        ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl(request.getImageUrl())
                .isMain(Boolean.TRUE.equals(request.getIsMain()))
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 1)
                .build();

        productImageRepository.save(image);
        return toResponse(product);
    }

    @Override
    @Transactional
    public void deleteImage(Integer productId, Integer imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", imageId));

        if (!image.getProduct().getProductId().equals(productId)) {
            throw new BadRequestException("Ảnh không thuộc sản phẩm này");
        }
        productImageRepository.delete(image);
    }

    @Override
    @Transactional
    public void setMainImage(Integer productId, Integer imageId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", productId);
        }
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", imageId));

        if (!image.getProduct().getProductId().equals(productId)) {
            throw new BadRequestException("Ảnh không thuộc sản phẩm này");
        }

        productImageRepository.clearMainImageByProductId(productId);
        image.setIsMain(true);
        productImageRepository.save(image);
    }
}