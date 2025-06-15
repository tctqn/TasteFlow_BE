package com.startup.tasteflowbe.mapper;

import com.startup.tasteflowbe.dto.request.OrderRequestDTO;
import com.startup.tasteflowbe.dto.response.OrderResponseDTO;
import com.startup.tasteflowbe.model.Order;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "orderCode", ignore = true) // Không map từ request
    @Mapping(target = "orderDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "user", ignore = true)
    Order toEntity(OrderRequestDTO dto);

    OrderResponseDTO toDto(Order order);
}
