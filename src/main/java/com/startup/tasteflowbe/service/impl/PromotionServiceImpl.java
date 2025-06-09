package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.Promotion;
import com.startup.tasteflowbe.repository.PromotionRepository;
import com.startup.tasteflowbe.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;

    @Override
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    @Override
    public Optional<Promotion> getPromotionById(Long id) {
        return promotionRepository.findById(id);
    }

    @Override
    public Promotion createPromotion(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    @Override
    public Promotion updatePromotion(Long id, Promotion promotion) {
        return promotionRepository.findById(id)
                .map(existingPromotion -> {
                    existingPromotion.setName(promotion.getName());
                    existingPromotion.setDescription(promotion.getDescription());
                    existingPromotion.setDiscountPercentage(promotion.getDiscountPercentage());
                    existingPromotion.setStartDate(promotion.getStartDate());
                    existingPromotion.setEndDate(promotion.getEndDate());
                    return promotionRepository.save(existingPromotion);
                })
                .orElseThrow(() -> new RuntimeException("Promotion not found with id " + id));
    }

    @Override
    public void deletePromotion(Long id) {
        promotionRepository.deleteById(id);
    }
}
