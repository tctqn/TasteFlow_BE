package com.startup.tasteflowbe.mapper;

import com.startup.tasteflowbe.dto.response.UserDTO;
import com.startup.tasteflowbe.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring") // Nếu dùng Spring, inject được luôn
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDTO toDTO(User user);
}
