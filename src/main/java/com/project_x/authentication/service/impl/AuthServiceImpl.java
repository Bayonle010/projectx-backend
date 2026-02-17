package com.project_x.authentication.service.impl;

import com.project_x.authentication.builder.UserResponseBuilder;
import com.project_x.authentication.dto.request.LoginRequest;
import com.project_x.authentication.dto.request.RefreshTokenRequest;
import com.project_x.authentication.dto.request.RegistrationRequest;
import com.project_x.authentication.dto.response.AuthResponse;
import com.project_x.authentication.dto.response.UserResponse;
import com.project_x.authentication.service.AuthService;
import com.project_x.authentication.token.service.TokenService;
import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.core.security.JwtUtil;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final static Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthServiceImpl(UserRepository userRepository, RoleService roleService, AuthenticationManager authenticationManager, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
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

    @Override
    public ResponseEntity<ApiResponse> authenticateUser(LoginRequest request) {
        final String email = request.email().toLowerCase().trim();
        final String password = request.password();

        // 1) Authenticate: any bad email/password -> 400
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseUtil.error(99, "invalid email or password", null, null));
        }

        // 2) Fetch user AFTER successful auth
        final User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseUtil.error(99, "invalid email or password", null, null));
        }

        if (!UserType.USER.equals(user.getUserType())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResponseUtil.error(99, "Access denied", null, null));
        }


        // 3) Business rules
        //         TODO : send email to the user
//        if (!user.isEmailVerified()) {

//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(ResponseUtil.success(0, String.format("OTP sent to %s ", email), "", "", null));
//        }

        // 4) Generate tokens & persist refresh
        final String accessToken = jwtUtil.generateAccessTokenForUser(user);
        final String refreshToken = jwtUtil.generateRefreshTokenForUser(user);

        tokenService.saveRefreshToken(user, refreshToken);

         UserResponse userInfo = UserResponseBuilder.toDto(user);

        AuthResponse payload = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userResponse(userInfo)
                .build();

        return ResponseEntity.ok(ResponseUtil.success(0, "Login Successful", "User Authenticated Successfully", payload, null));
    }

    @Override
    public ResponseEntity<ApiResponse> refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        Jwt jwt = jwtUtil.decodeJwt(refreshToken); // validates signature & expiry
        String email = jwt.getSubject();


        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate against DB (throws if invalid)
        tokenService.isRefreshTokenValid(refreshToken, user);

        // Generate new access token
        String accessToken = jwtUtil.generateAccessTokenForUser(user);

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success(0, "success", "new access token generated successfully", authResponse, null));
    }
}
