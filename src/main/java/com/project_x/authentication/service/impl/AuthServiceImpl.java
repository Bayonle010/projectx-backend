package com.project_x.authentication.service.impl;

import com.project_x.authentication.builder.UserResponseBuilder;
import com.project_x.authentication.dto.request.RegistrationRequest;
import com.project_x.authentication.dto.response.UserResponse;
import com.project_x.authentication.service.AuthService;
import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.core.util.NumberUtil;
import com.project_x.role.Role;
import com.project_x.role.service.RoleService;
import com.project_x.user.entity.User;
import com.project_x.user.enums.UserType;
import com.project_x.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final static Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;
    private final RoleService roleService;

    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ResponseEntity<ApiResponse> registerUser(RegistrationRequest request) {
        String formatedEmailFromRequest = request.email().toLowerCase().trim();
        String formattedFirstname = StringUtils.capitalize(request.firstName().trim().toLowerCase());
        String formattedLastname = StringUtils.capitalize(request.lastName().trim().toLowerCase());


        if (userRepository.existsByEmail(formatedEmailFromRequest)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseUtil.error(99, "email already exist", "register with a new email", null));
        }



        // Encode password
        String encodedPassword = passwordEncoder.encode(request.password());

        // Create new user
        User newUser = User.builder()
                .firstname(formattedFirstname)
                .lastname(formattedLastname)
                .email(formatedEmailFromRequest)
                .phoneNumber(NumberUtil.normalizePhoneNumber(request.phoneNumber()))
                .password(encodedPassword)
                .userType(UserType.USER)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .isEnabled(true)
                .build();

        // Initialize roles if null
        if (newUser.getRoles() == null) {
            newUser.setRoles(new HashSet<>());
        }

        Optional<Role> userRole = roleService.findByAuthority("ROLE_USER");
        userRole.ifPresent(role -> newUser.getRoles().add(role));

        // Save user to database
        log.info("Registering new new user: {}", newUser);
        User savedUser = userRepository.save(newUser);

        UserResponse response = UserResponseBuilder.toDto(savedUser);


        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseUtil.success(0, "Registration successful", "user registered successfully", response, "" )
        );
    }
}
