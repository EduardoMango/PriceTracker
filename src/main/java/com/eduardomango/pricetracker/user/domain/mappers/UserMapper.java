package com.eduardomango.pricetracker.user.domain.mappers;

import com.eduardomango.pricetracker.common.model.IMapper;
import com.eduardomango.pricetracker.user.domain.UserEntity;
import com.eduardomango.pricetracker.user.domain.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper extends IMapper<UserEntity, UserDTO> {

    @Mapping(source = "externalId", target = "userId")
    UserDTO toDTO(UserEntity userEntity);
    UserEntity toEntity(UserDTO userDTO);
}
