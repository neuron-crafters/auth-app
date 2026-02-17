package com.neuroncrafters.auth_app.services;

import com.neuroncrafters.auth_app.dtos.UserDto;
import com.neuroncrafters.auth_app.entities.Provider;
import com.neuroncrafters.auth_app.entities.User;
import com.neuroncrafters.auth_app.exceptions.ResourceNotFoundException;
import com.neuroncrafters.auth_app.helpers.UserHelper;
import com.neuroncrafters.auth_app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.databind.cfg.MapperBuilder;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository; // Auto injection - as we made field final no need to write @Autowired
    private final ModelMapper modelMapper;
    private final MapperBuilder mapperBuilder;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (userDto.getPassword() == null || userDto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with given email already exists");
        }

        User user = modelMapper.map(userDto, User.class);
        user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);
        // TODO: Assign role to new user for authorization
        User savedUser = userRepository.save(user);

        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with given email"));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto updateUser(UserDto user, String userId) {
        UUID uId = UUID.fromString(userId);
        User existingUser = userRepository.findById(uId).orElseThrow(() -> new ResourceNotFoundException("User not found with given ID"));
        // we are not going to allow changing email id.
        if (user.getName() != null && !user.getName().isBlank()) existingUser.setName(user.getName());
        if(user.getImage() != null && !user.getImage().isBlank()) existingUser.setImage(user.getImage());
        if (user.getProvider() != null) existingUser.setProvider(user.getProvider());
        // TODO: change password update logic...
        if (user.getPassword() != null && !user.getPassword().isBlank()) existingUser.setPassword(user.getPassword());
        existingUser.setEnable(user.isEnable());
        User updatedUser = userRepository.save(existingUser);
        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public void deleteUser(String userId) {
        UUID uId = UserHelper.parseUUID(userId);
        User user = userRepository.findById(uId).orElseThrow(() -> new ResourceNotFoundException("User not found with given ID"));
        userRepository.delete(user);
    }

    @Override
    public UserDto getUserById(String userId) {
        User user = userRepository.findById(UserHelper.parseUUID(userId)).orElseThrow(() -> new ResourceNotFoundException("User not found with given ID"));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<UserDto> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
    }
}
