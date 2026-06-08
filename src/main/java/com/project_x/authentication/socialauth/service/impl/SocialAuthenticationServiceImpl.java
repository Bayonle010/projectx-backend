package com.project_x.authentication.socialauth.service.impl;

import com.project_x.authentication.socialauth.entity.Oauth2AuthenticationProvider;
import com.project_x.authentication.socialauth.repository.Oauth2ProviderRepository;
import com.project_x.authentication.socialauth.service.SocialAuthenticationService;
import com.project_x.authentication.socialauth.service.SocialUserInfo;
import com.project_x.core.exception.BadRequestException;
import com.project_x.role.RoleRepository;
import com.project_x.user.entity.User;
import com.project_x.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Random;
import java.util.UUID;

@Service
public class SocialAuthenticationServiceImpl implements SocialAuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(SocialAuthenticationServiceImpl.class);

    private final Oauth2ProviderRepository oauth2ProviderRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public SocialAuthenticationServiceImpl(Oauth2ProviderRepository oauth2ProviderRepository, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.oauth2ProviderRepository = oauth2ProviderRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User handleSocialLogin(SocialUserInfo socialUserInfo) {
        validateRequiredFields(socialUserInfo);

        log.info("Handling social login for provider: {}, email: {}",
                socialUserInfo.getProviderType(), socialUserInfo.getEmail());

        // 1. Check if this exact provider account is already linked
        var linkedProviderOpt = oauth2ProviderRepository.findByProviderTypeAndProviderId(
                socialUserInfo.getProviderType(),
                socialUserInfo.getProviderId()
        );

        if (linkedProviderOpt.isPresent()) {
            log.info("Existing social link found. Signing user in.");
            return linkedProviderOpt.get().getUser();
        }

        // 2. Check if user exists by email
        var existingUserOpt = userRepository.findByEmail((normalizeEmail(socialUserInfo.getEmail())));

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            // Recommended policy:
            // only auto-link if provider email is verified
            if (!socialUserInfo.isEmailVerified()) {
                throw new BadRequestException("Social account email is not verified. Cannot link account automatically.");
            }

            log.info("Existing local user found by email. Linking social provider.");
            linkProvider(existingUser, socialUserInfo);
            return existingUser;
        }

        // 3. Create new user and link social provider
        log.info("No existing user found. Creating new social user.");

        User newUser = createNewSocialUser(socialUserInfo);
        
        linkProvider(newUser, socialUserInfo);

        return newUser;
    }

    private void validateRequiredFields(SocialUserInfo socialUserInfo) {
        if (socialUserInfo.getProviderType() == null) {
            throw new IllegalArgumentException("Provider type is required");
        }

        if (socialUserInfo.getProviderId() == null || socialUserInfo.getProviderId().isBlank()) {
            throw new IllegalArgumentException("Provider ID is required");
        }

        if (socialUserInfo.getEmail() == null || socialUserInfo.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
    }

    private User createNewSocialUser(SocialUserInfo socialUserInfo) {
        User user = new User();
        user.setEmail(normalizeEmail(socialUserInfo.getEmail()));
        user.setFirstname(resolveFirstName(socialUserInfo));
        user.setLastname(resolveLastName(socialUserInfo));
        user.setUsername(generateUniqueUsername(
                resolveFirstName(socialUserInfo),
                resolveLastName(socialUserInfo)
        ));

        // Random password because password auth is not used for social-only signup
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        roleRepository.findByAuthority("ROLE_USER")
                .ifPresent(role -> user.getRoles().add(role));

        return userRepository.save(user);
    }

    private void linkProvider(User user, SocialUserInfo socialUserInfo) {
        Oauth2AuthenticationProvider authProvider = Oauth2AuthenticationProvider.builder()
                .providerType(socialUserInfo.getProviderType())
                .providerId(socialUserInfo.getProviderId())
                .providerEmail(normalizeEmail(socialUserInfo.getEmail()))
                .emailVerified(socialUserInfo.isEmailVerified())
                .user(user)
                .build();

        oauth2ProviderRepository.save(authProvider);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String resolveFirstName(SocialUserInfo socialUserInfo) {
        if (socialUserInfo.getFirstName() != null && !socialUserInfo.getFirstName().isBlank()) {
            return socialUserInfo.getFirstName().trim();
        }

        if (socialUserInfo.getDisplayName() != null && !socialUserInfo.getDisplayName().isBlank()) {
            return socialUserInfo.getDisplayName().trim();
        }

        return "User";
    }

    private String resolveLastName(SocialUserInfo socialUserInfo) {
        if (socialUserInfo.getLastName() != null && !socialUserInfo.getLastName().isBlank()) {
            return socialUserInfo.getLastName().trim();
        }

        return "Social";
    }

    private String generateUniqueUsername(String firstName, String lastName) {
        String cleanFirstName = (firstName == null || firstName.isBlank())
                ? "USER"
                : firstName.replaceAll("\\s+", "");

        String lastInitial = (lastName == null || lastName.isBlank())
                ? "X"
                : lastName.substring(0, 1);

        int randomNumber = 100 + new Random().nextInt(900);

        return (cleanFirstName + lastInitial + randomNumber).toUpperCase(Locale.ROOT);
    }
}
