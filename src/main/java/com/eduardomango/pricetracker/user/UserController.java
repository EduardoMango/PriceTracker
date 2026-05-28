package com.eduardomango.pricetracker.user;

import com.eduardomango.pricetracker.user.domain.dto.NewUserDTO;
import com.eduardomango.pricetracker.user.domain.dto.UserDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final IUserService userService;

    @GetMapping
    ResponseEntity<List<UserDTO>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{userId}")
    ResponseEntity<UserDTO> findById(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.findById(userId));
    }

    @PutMapping("/{userId}")
    ResponseEntity<UserDTO> update(@PathVariable UUID userId, @RequestBody NewUserDTO newUserDTO) {
        return ResponseEntity.ok(userService.update(userId, newUserDTO));
    }

    @DeleteMapping("/{userId}")
    ResponseEntity<Void> delete(@PathVariable UUID userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
