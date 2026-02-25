package com.neuroncrafters.auth_app.services.impl;

import com.neuroncrafters.auth_app.dtos.UserDto;
import com.neuroncrafters.auth_app.services.AuthService;
import com.neuroncrafters.auth_app.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    @Override
    public UserDto registerUser(UserDto user) {

        // Other logics
        // Validating email
        // Validating password
        // default roles
        UserDto createdUser = userService.createUser(user);
        return createdUser;
    }

    @Override
    public UserDto loginUser(UserDto user) {
        return null;
    }
}
