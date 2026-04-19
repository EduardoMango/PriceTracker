package com.eduardomango.pricetracker.user.domain.mappers;

import com.eduardomango.pricetracker.common.model.IMapper;
import com.eduardomango.pricetracker.user.domain.UserEntity;
import com.eduardomango.pricetracker.user.domain.dto.NewUserDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NewUserMapper extends IMapper<UserEntity, NewUserDTO> {

    UserEntity toEntity(NewUserDTO newUserDTO);
    NewUserDTO toDTO(UserEntity userEntity);
}
