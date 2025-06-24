package com.startup.tasteflowbe.mapper;

import com.startup.tasteflowbe.dto.request.OrderRequestDTO;
import com.startup.tasteflowbe.dto.response.OrderResponseDTO;
import com.startup.tasteflowbe.enums.OrderStatus;
import com.startup.tasteflowbe.model.Order;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "orderCode", ignore = true)
    @Mapping(target = "orderDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "user", ignore = true)
    Order toEntity(OrderRequestDTO dto);

    @Mapping(target = "items", source = "orderItems")
    @Mapping(source = "status", target = "status", qualifiedByName = "mapStatus")
    @Mapping(source = "invoice.invoiceCompanyName", target = "invoiceCompanyName")
    @Mapping(source = "invoice.invoiceEmail", target = "invoiceEmail")
    @Mapping(source = "invoice.invoiceTaxCode", target = "invoiceTaxCode")
    @Mapping(source = "invoice.invoiceCompanyAddress", target = "invoiceCompanyAddress")
    @Mapping(source = "invoice.invoiceUrl", target = "invoiceUrl")
    @Mapping(source = "invoice.issuedAt", target = "invoiceIssuedAt")
    OrderResponseDTO toDto(Order order);

    @Named("mapStatus")
    static String mapStatus(OrderStatus status) {
        return status.name();
    }
}
