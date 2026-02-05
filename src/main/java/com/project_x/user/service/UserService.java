package com.project_x.user.service;

import com.project_x.user.entity.User;

public interface UserService {
    User findUserByEmail(String email);
}
