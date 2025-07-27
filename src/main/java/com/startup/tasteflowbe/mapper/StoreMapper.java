package com.startup.tasteflowbe.mapper;

import com.startup.tasteflowbe.dto.response.StoreResponseDTO;
import com.startup.tasteflowbe.model.Store;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface StoreMapper {

    StoreMapper INSTANCE = Mappers.getMapper(StoreMapper.class);

    @Mapping(source = "manager.userId", target = "managerId")
    StoreResponseDTO toDto(Store store);
}
