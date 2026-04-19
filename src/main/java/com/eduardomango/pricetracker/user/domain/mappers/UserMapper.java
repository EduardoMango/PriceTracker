package com.eduardomango.pricetracker.user.domain.mappers;

import com.eduardomango.pricetracker.common.model.IMapper;
import com.eduardomango.pricetracker.user.domain.UserEntity;
import com.eduardomango.pricetracker.user.domain.dto.UserDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper extends IMapper<UserEntity, UserDTO> {
    UserDTO toDTO(UserEntity userEntity);
    UserEntity toEntity(UserDTO userDTO);
}
