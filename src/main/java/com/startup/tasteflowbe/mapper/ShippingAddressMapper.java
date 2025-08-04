package com.startup.tasteflowbe.mapper;

import com.startup.tasteflowbe.dto.request.ShippingAddressRequestDTO;
import com.startup.tasteflowbe.model.ShippingAddress;
import com.startup.tasteflowbe.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ShippingAddressMapper {

    ShippingAddressMapper INSTANCE = Mappers.getMapper(ShippingAddressMapper.class);

    @Mapping(target = "addressId", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "recipientName", source = "dto.recipientName")
    @Mapping(target = "phone", source = "dto.phone")
    @Mapping(target = "isDefault", source = "dto.isDefault")
    @Mapping(
            target = "addressLine",
            expression = "java(buildFullAddress(dto))"
    )
    ShippingAddress toEntity(ShippingAddressRequestDTO dto, User user);

    default String buildFullAddress(ShippingAddressRequestDTO dto) {
        StringBuilder sb = new StringBuilder();
        if (dto.getAddressLine() != null) sb.append(dto.getAddressLine());
        if (dto.getWard() != null) sb.append(", ").append(dto.getWard());
        if (dto.getDistrict() != null) sb.append(", ").append(dto.getDistrict());
        if (dto.getProvince() != null) sb.append(", ").append(dto.getProvince());
        return sb.toString();
    }
}
