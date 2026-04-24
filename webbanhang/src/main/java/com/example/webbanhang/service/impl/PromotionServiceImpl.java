package com.example.webbanhang.service.impl;

import com.example.webbanhang.dto.request.PromotionRequest;
import com.example.webbanhang.dto.response.PromotionResponse;
import com.example.webbanhang.entity.Product;
import com.example.webbanhang.entity.Promotion;
import com.example.webbanhang.entity.ProductPromotion;
import com.example.webbanhang.repository.ProductPromotionRepository;
import com.example.webbanhang.repository.ProductRepository;
import com.example.webbanhang.repository.PromotionRepository;
import com.example.webbanhang.service.PromotionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final ProductPromotionRepository productPromotionRepository;

    @Override
    public PromotionResponse create(PromotionRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        Promotion promotion = Promotion.builder()
                .promotionName(request.getPromotionName())
                .discountPercent(request.getDiscountPercent())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        return toResponse(promotionRepository.save(promotion));
    }

    @Override
    public PromotionResponse update(Integer id, PromotionRequest request) {
        Promotion promotion = findOrThrow(id);
        promotion.setPromotionName(request.getPromotionName());
        promotion.setDiscountPercent(request.getDiscountPercent());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        return toResponse(promotionRepository.save(promotion));
    }

    @Override
    public void delete(Integer id) {
        findOrThrow(id);
        promotionRepository.deleteById(id);
    }

    @Override
    public List<PromotionResponse> getAll() {
        return promotionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PromotionResponse> getActive() {
        return promotionRepository.findActivePromotions(LocalDateTime.now()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void applyToProduct(Integer productId, Integer promotionId) {
        if (productPromotionRepository.existsByProductProductIdAndPromotionPromotionId(productId, promotionId)) {
            throw new IllegalArgumentException("Khuyến mãi đã được áp dụng cho sản phẩm này");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm"));
        Promotion promotion = findOrThrow(promotionId);

        ProductPromotion pp = ProductPromotion.builder()
                .product(product)
                .promotion(promotion)
                .build();
        productPromotionRepository.save(pp);
    }

    @Override
    @Transactional
    public void removeFromProduct(Integer productId, Integer promotionId) {
        productPromotionRepository.deleteByProductProductIdAndPromotionPromotionId(productId, promotionId);
    }

    @Override
    public List<PromotionResponse> getByProduct(Integer productId) {
        return productPromotionRepository.findByProductProductId(productId).stream()
                .map(pp -> toResponse(pp.getPromotion()))
                .collect(Collectors.toList());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Promotion findOrThrow(Integer id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khuyến mãi id: " + id));
    }

    private PromotionResponse toResponse(Promotion p) {
        LocalDateTime now = LocalDateTime.now();
        boolean active = p.getStartDate() != null && p.getEndDate() != null
                && !now.isBefore(p.getStartDate()) && !now.isAfter(p.getEndDate());
        return PromotionResponse.builder()
                .promotionId(p.getPromotionId())
                .promotionName(p.getPromotionName())
                .discountPercent(p.getDiscountPercent())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .active(active)
                .build();
    }
}