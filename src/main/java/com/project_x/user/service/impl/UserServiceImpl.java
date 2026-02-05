package com.project_x.user.service.impl;

import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.user.entity.User;
import com.project_x.user.repository.UserRepository;
import com.project_x.user.service.UserService;
import org.springframework.stereotype.Service;

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
}
