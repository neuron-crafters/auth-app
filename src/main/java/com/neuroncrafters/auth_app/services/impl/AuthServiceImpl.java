package com.neuroncrafters.auth_app.services.impl;

import com.neuroncrafters.auth_app.dtos.UserDto;
import com.neuroncrafters.auth_app.services.AuthService;
import com.neuroncrafters.auth_app.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserDto registerUser(UserDto userDto) {
        // Other logics
        // Validating email
        // Validating password
        // default roles
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return userService.createUser(userDto);
    }

    @Override
    public UserDto loginUser(UserDto user) {
        return null;
    }
}
