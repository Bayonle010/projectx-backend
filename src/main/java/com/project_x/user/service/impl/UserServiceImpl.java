package com.project_x.user.service.impl;

import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.user.entity.User;
import com.project_x.user.repository.UserRepository;
import com.project_x.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("user not found"));
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public User fetchAuthenticatedUser(AuthenticationIdentity authenticationIdentity){
        return userRepository.findById(UUID.fromString(authenticationIdentity.getId())).orElseThrow(()-> new ResourceNotFoundException("user not found"));
    }
}
