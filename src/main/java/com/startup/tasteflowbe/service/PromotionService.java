package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.dto.request.PromotionRequestDTO;
import com.startup.tasteflowbe.dto.response.PromotionResponseDTO;
import com.startup.tasteflowbe.model.Promotion;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface PromotionService {
    List<PromotionResponseDTO> getAllPromotions();
    Optional<Promotion> getPromotionById(Long id);
    void createPromotion(PromotionRequestDTO dto, MultipartFile imageFile);
    Promotion updatePromotion(Long id, Promotion promotion);
    void deletePromotion(Long id);
}
