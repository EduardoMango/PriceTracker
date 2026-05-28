package com.eduardomango.pricetracker.user;

import com.eduardomango.pricetracker.common.exceptions.EntityNotFoundException;
import com.eduardomango.pricetracker.common.model.IMapper;
import com.eduardomango.pricetracker.user.domain.UserEntity;
import com.eduardomango.pricetracker.user.domain.dto.NewUserDTO;
import com.eduardomango.pricetracker.user.domain.dto.UserDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService implements IUserService{

    private final UserRepository userRepository;

    private final IMapper<UserEntity, NewUserDTO> newUserMapper;
    private final IMapper<UserEntity, UserDTO> userMapper;

    @Override
    public UserDTO save(NewUserDTO newUserDTO) {
        System.out.println(newUserDTO);
        UserEntity saved = userRepository.save(newUserMapper.toEntity(newUserDTO));
        System.out.println(saved);

        return userMapper.toDTO(saved);
    }

//    @Override
//    @Transactional
//    public UserDTO save(NewAccountRequest newAccountRequest) {
//
//        NewUserDTO newUser = new NewUserDTO(new Email(newAccountRequest.email()));
//
//        UserEntity saved = userRepository.save(newUserMapper.toEntity(newUser));
//
//        CredentialsEntity newCredentials = CredentialsEntity.builder().roles(Set.of(roleRepository.findById(1L).orElseThrow()))
//                .enabled(true)
//                .username(newAccountRequest.username())
//                .password(passwordEncoder.encode(newAccountRequest.password()))
//                .build();
//
//        credentialsRepository.save(newCredentials);
//
//        return userMapper.toDTO(saved);
//    }

    @Override
    public void delete(UUID userId) {
        UserEntity user = userRepository.findByExternalId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User","User was not found","userId", userId.toString()));

        userRepository.delete(user);
    }

    @Override
    public UserDTO update(UUID userId, NewUserDTO newUserDTO) {
        UserEntity user = userRepository.findByExternalId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User", "User was not found", "userId", userId.toString()
                ));

        user.setEmail(newUserDTO.email());

        UserEntity saved = userRepository.save(user);

        return userMapper.toDTO(saved);
    }

    @Override
    public UserDTO findById(UUID userId) {
        return userRepository.findByExternalId(userId)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User", "User was not found", "userId", userId.toString()
                ));
    }

    @Override
    public List<UserDTO> findAll() {
        return userRepository.findAll().stream().map(userMapper::toDTO).toList();
    }
}
