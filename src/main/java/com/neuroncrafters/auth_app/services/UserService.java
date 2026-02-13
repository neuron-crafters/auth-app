package com.neuroncrafters.auth_app.services;

import com.neuroncrafters.auth_app.dtos.UserDto;

public interface UserService {
    UserDto createUser(UserDto user);

    UserDto getUserByEmail(String email);

    UserDto updateUser(UserDto user, String userId);

    void deleteUser(String userId);

    UserDto getUserById(String userId);

    Iterable<UserDto> getAllUsers();
}
