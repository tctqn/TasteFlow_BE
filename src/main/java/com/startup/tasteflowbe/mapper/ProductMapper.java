package com.startup.tasteflowbe.mapper;

import com.startup.tasteflowbe.dto.request.ProductRequestDTO;
import com.startup.tasteflowbe.dto.response.ProductResponseDTO;
import com.startup.tasteflowbe.model.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = PromotionMapper.class)
public interface ProductMapper {

    // ✅ Map category.name → categoryName và tự động map promotions → promotions
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "promotions", target = "promotions")
    ProductResponseDTO toResponse(Product product);

    // ✅ Mapping từ DTO → entity để tạo
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "category", ignore = true)    // category sẽ được set trong service
    @Mapping(target = "promotions", ignore = true) // promotions sẽ được set trong service
    Product toEntity(ProductRequestDTO dto);

    // ✅ Mapping từ DTO → entity để cập nhật (sử dụng @MappingTarget)
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "promotions", ignore = true)
    void updateEntityFromDTO(ProductRequestDTO dto, @MappingTarget Product entity);
}
