package com.example.webbanhang.service;

import com.example.webbanhang.dto.request.PromotionRequest;
import com.example.webbanhang.dto.response.PromotionResponse;
import java.util.List;

public interface PromotionService {
    PromotionResponse create(PromotionRequest request);
    PromotionResponse update(Integer id, PromotionRequest request);
    void delete(Integer id);
    List<PromotionResponse> getAll();
    List<PromotionResponse> getActive();
    void applyToProduct(Integer productId, Integer promotionId);
    void removeFromProduct(Integer productId, Integer promotionId);
    List<PromotionResponse> getByProduct(Integer productId);
}