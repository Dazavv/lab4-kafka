package com.hs.lab4.userservice.mapper;

import com.hs.lab4.userservice.dto.responses.UserDto;
import com.hs.lab4.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    UserDto toUserDto(User user);
    List<UserDto> toUserDtoList(List<User> users);
}
