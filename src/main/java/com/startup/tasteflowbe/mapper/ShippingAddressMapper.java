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
    @Mapping(target = "addressLine", expression = "java(dto.getAddressLine() + \", \" + dto.getWard() + \", \" + dto.getDistrict() + \", \" + dto.getProvince())")
    @Mapping(target = "isDefault", source = "dto.isDefault")
    ShippingAddress toEntity(ShippingAddressRequestDTO dto, User user);
}
