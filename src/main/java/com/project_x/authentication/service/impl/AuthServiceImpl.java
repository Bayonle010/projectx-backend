package com.project_x.authentication.service.impl;

import com.project_x.authentication.dto.request.RegistrationRequest;
import com.project_x.authentication.service.AuthService;
import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.user.entity.User;
import com.project_x.user.enums.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final static Logger log = LoggerFactory.getLogger(AuthServiceImpl.class)
    private final UserRepository userRepository;

    @Override
    public ResponseEntity<ApiResponse> registerUser(RegistrationRequest request) {
        String formatedEmailFromRequest = request.email().toLowerCase().trim();
        String formattedFirstname = StringUtils.capitalize(request.firstName().trim().toLowerCase());
        String formattedLastname = StringUtils.capitalize(request.lastName().trim().toLowerCase());


        if (userRepository.existsByEmail(formatedEmailFromRequest)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ResponseUtil.error(99, "user already exist", null, null));
        }



        // Encode password
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // Create new user
        User newUser = User.builder()
                .firstname(formattedFirstname)
                .lastname(formattedLastname)
                .email(formatedEmailFromRequest)
                .phoneNumber(NumberUtils.normalizePhoneNumber(request.getPhoneNumber()))
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

        Optional<Role> userRole = roleRepository.findByAuthority("ROLE_USER");
        userRole.ifPresent(role -> newUser.getRoles().add(role));

        // Save user to database
        log.info("Registering new new user: {}", newUser);
        User savedUser = userRepository.save(newUser);

        UserResponseBuilder response = null;


        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseUtil.success(0, "Registration successful", "user registered successfully", response, "" )
        );
    }
}
