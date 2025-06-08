package com.startup.tasteflowbe.mapper;

import com.startup.tasteflowbe.dto.response.PromotionResponseDTO;
import com.startup.tasteflowbe.model.Promotion;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    PromotionResponseDTO toDto(Promotion promotion);

    default List<PromotionResponseDTO> toPromotionDTOs(List<Promotion> promotions) {
        if (promotions == null) return List.of();
        return promotions.stream()
                .map(this::toDto)
                .toList();
    }
}
