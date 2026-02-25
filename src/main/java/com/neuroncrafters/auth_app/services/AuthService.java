package com.neuroncrafters.auth_app.services;

import com.neuroncrafters.auth_app.dtos.UserDto;

public interface AuthService {
    UserDto registerUser(UserDto user);
    UserDto loginUser(UserDto user);
}
