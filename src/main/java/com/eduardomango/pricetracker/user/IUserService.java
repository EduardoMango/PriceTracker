package com.eduardomango.pricetracker.user;

import com.eduardomango.pricetracker.auth.dto.NewAccountRequest;
import com.eduardomango.pricetracker.user.domain.dto.NewUserDTO;
import com.eduardomango.pricetracker.user.domain.dto.UserDTO;

import java.util.List;
import java.util.UUID;

public interface IUserService {
    UserDTO save(NewAccountRequest newAccountRequest);
    void delete(UUID userId);
    UserDTO update(UUID userId, NewUserDTO newUserDTO);
    UserDTO findById(UUID userId);
    List<UserDTO> findAll();
}
