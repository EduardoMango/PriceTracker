package com.eduardomango.pricetracker.auth;

import com.eduardomango.pricetracker.auth.dto.AuthRequest;
import com.eduardomango.pricetracker.auth.dto.AuthResponse;
import com.eduardomango.pricetracker.auth.dto.NewAccountRequest;
import com.eduardomango.pricetracker.auth.jwt.JwtService;
import com.eduardomango.pricetracker.common.model.Email;
import com.eduardomango.pricetracker.user.UserService;
import com.eduardomango.pricetracker.user.domain.dto.NewUserDTO;
import com.eduardomango.pricetracker.user.domain.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody AuthRequest authRequest){
        UserDetails user = authService.authenticate(authRequest);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody NewAccountRequest newAccountRequest){
        return new ResponseEntity<>(userService.save(newAccountRequest), HttpStatus.CREATED);
    }

}
