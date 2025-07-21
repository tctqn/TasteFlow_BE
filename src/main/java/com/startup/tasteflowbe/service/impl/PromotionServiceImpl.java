package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.dto.request.PromotionRequestDTO;
import com.startup.tasteflowbe.dto.response.PromotionResponseDTO;
import com.startup.tasteflowbe.enums.DiscountType;
import com.startup.tasteflowbe.mapper.PromotionMapper;
import com.startup.tasteflowbe.model.Category;
import com.startup.tasteflowbe.model.Product;
import com.startup.tasteflowbe.model.Promotion;
import com.startup.tasteflowbe.model.Store;
import com.startup.tasteflowbe.repository.CategoryRepository;
import com.startup.tasteflowbe.repository.ProductRepository;
import com.startup.tasteflowbe.repository.PromotionRepository;
import com.startup.tasteflowbe.repository.StoreRepository;
import com.startup.tasteflowbe.service.PromotionService;
import com.startup.tasteflowbe.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final PromotionMapper promotionMapper;
    private final S3Service s3Service;

    @Override
    public List<PromotionResponseDTO> getAllPromotions() {
        return promotionMapper.toPromotionDTOs(promotionRepository.findAll());
    }

    @Override
    public Optional<Promotion> getPromotionById(Long id) {
        return promotionRepository.findById(id);
    }

    @Override
    public void createPromotion(PromotionRequestDTO dto, MultipartFile imageFile) {
        Promotion promotion = promotionMapper.toEntity(dto);

        // Upload ảnh nếu có
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = s3Service.uploadImage(imageFile);
            promotion.setImageUrl(imageUrl);
        }

        if (dto.getPromotionId() != null && imageFile == null) {
            Promotion existingPromotion = promotionRepository.findById(dto.getPromotionId())
                    .orElseThrow(() -> new IllegalArgumentException("Promotion not found with id " + dto.getPromotionId()));
            promotion.setImageUrl(existingPromotion.getImageUrl()); // Nếu có promotionId, sử dụng imageUrl từ DTO
        }

        if (promotion.getDiscountType() == DiscountType.AMOUNT) {
            promotion.setDiscountPercentage(null); // Đặt discountPercentage là null nếu discountType là AMOUNT
        } else if (promotion.getDiscountType() == DiscountType.PERCENT) {
            promotion.setDiscountAmount(null); // Đặt discountAmount là null nếu discountType là PERCENT
        }

        if (promotion.getDiscountType() == DiscountType.AMOUNT) {
            if (promotion.getDiscountAmount() == null || promotion.getDiscountAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Discount amount must be greater than zero for AMOUNT discount type.");
            }
        } else if (promotion.getDiscountType() == DiscountType.PERCENT) {
            if (promotion.getDiscountPercentage() == null || promotion.getDiscountPercentage().compareTo(BigDecimal.ZERO) <= 0 || promotion.getDiscountPercentage().compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException("Discount percentage must be between 0 and 100 for PERCENT discount type.");
            }
        } else {
            throw new IllegalArgumentException("Invalid discount type.");
        }

        // Gắn quan hệ
        if (dto.getApplicableProducts() != null && !dto.getApplicableProducts().isEmpty()) {
            Set<Product> products = new HashSet<>(productRepository.findAllById(dto.getApplicableProducts()));
            promotion.setApplicableProducts(products);
        }

        if (dto.getApplicableCategories() != null && !dto.getApplicableCategories().isEmpty()) {
            Set<Category> categories = new HashSet<>(categoryRepository.findAllById(dto.getApplicableCategories()));
            promotion.setApplicableCategories(categories);
        }

        if (dto.getApplicableStores() != null && !dto.getApplicableStores().isEmpty()) {
            Set<Store> stores = new HashSet<>(storeRepository.findAllById(dto.getApplicableStores()));
            promotion.setApplicableStores(stores);
        }

        promotionRepository.save(promotion);
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
