package com.startup.tasteflowbe.mapper;

import com.startup.tasteflowbe.dto.request.PromotionRequestDTO;
import com.startup.tasteflowbe.dto.response.PromotionResponseDTO;
import com.startup.tasteflowbe.enums.DiscountType;
import com.startup.tasteflowbe.model.Category;
import com.startup.tasteflowbe.model.Product;
import com.startup.tasteflowbe.model.Promotion;
import com.startup.tasteflowbe.model.Store;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    @Mapping(target = "applicableProductIds", source = "applicableProducts", qualifiedByName = "mapProductIds")
    @Mapping(target = "applicableCategoryIds", source = "applicableCategories", qualifiedByName = "mapCategoryIds")
    @Mapping(target = "applicableStoreIds", source = "applicableStores", qualifiedByName = "mapStoreIds")
    @Mapping(source = "imageUrl", target = "imageUrl")
    PromotionResponseDTO toDto(Promotion promotion);

    default List<PromotionResponseDTO> toPromotionDTOs(List<Promotion> promotions) {
        if (promotions == null) return List.of();
        return promotions.stream()
                .map(this::toDto)
                .toList();
    }

    default Promotion toEntity(PromotionRequestDTO dto) {
        if (dto == null) return null;

        Promotion promotion = new Promotion();
        promotion.setPromotionId(dto.getPromotionId());
        promotion.setName(dto.getName());
        promotion.setDescription(dto.getDescription());

        if (dto.getDiscountType() != null) {
            promotion.setDiscountType(DiscountType.valueOf(dto.getDiscountType()));
        }

        promotion.setDiscountAmount(dto.getDiscountAmount());
        promotion.setDiscountPercentage(dto.getDiscountPercentage());

        if (dto.getStartDate() != null && dto.getStartTime() != null) {
            promotion.setStartDate(LocalDateTime.of(dto.getStartDate(), dto.getStartTime()));
        }

        if (dto.getEndDate() != null && dto.getEndTime() != null) {
            promotion.setEndDate(LocalDateTime.of(dto.getEndDate(), dto.getEndTime()));
        }

        return promotion;
    }

    @Named("mapProductIds")
    default Set<Long> mapProductIds(Set<Product> products) {
        if (products == null) return Set.of();
        return products.stream().map(Product::getProductId).collect(Collectors.toSet());
    }

    @Named("mapCategoryIds")
    default Set<Long> mapCategoryIds(Set<Category> categories) {
        if (categories == null) return Set.of();
        return categories.stream().map(Category::getCategoryId).collect(Collectors.toSet());
    }
    @Named("mapStoreIds")
    default Set<Long> mapStoreIds(Set<Store> stores) {
        if (stores == null) return Set.of();
        return stores.stream().map(Store::getStoreId).collect(Collectors.toSet());
    }
}
