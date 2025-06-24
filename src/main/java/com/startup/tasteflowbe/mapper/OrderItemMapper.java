package com.startup.tasteflowbe.mapper;

import com.startup.tasteflowbe.dto.response.OrderItemResponseDTO;
import com.startup.tasteflowbe.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    OrderItemMapper INSTANCE = Mappers.getMapper(OrderItemMapper.class);

    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productImageUrl", source = "product.imageUrl") // <-- MAPPING IMAGE
    @Mapping(target = "productUnitId", source = "productUnit.productUnitId")
    @Mapping(target = "productUnitName", source = "productUnit.unit.name")
    OrderItemResponseDTO toDto(OrderItem orderItem);
}
