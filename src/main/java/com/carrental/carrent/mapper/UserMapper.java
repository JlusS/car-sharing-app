package com.carrental.carrent.mapper;

import com.carrental.carrent.config.MapperConfig;
import com.carrental.carrent.dto.user.UserRegistrationRequestDto;
import com.carrental.carrent.dto.user.UserResponseDto;
import com.carrental.carrent.dto.user.UserUpdateRequestDto;
import com.carrental.carrent.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserResponseDto toUserResponseDto(User user);

    User toUserModel(UserRegistrationRequestDto userRegistrationRequestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateUserFromDto(
            UserUpdateRequestDto dto,
                           @MappingTarget User targetUser);
}
