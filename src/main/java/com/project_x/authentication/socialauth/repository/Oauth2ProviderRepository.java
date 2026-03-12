package com.project_x.authentication.socialauth.repository;

import com.project_x.authentication.socialauth.entity.Oauth2AuthenticationProvider;
import com.project_x.authentication.socialauth.enums.Oauth2ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface Oauth2ProviderRepository extends JpaRepository<Oauth2AuthenticationProvider, UUID> {

    Optional<Oauth2AuthenticationProvider> findByProviderTypeAndProviderId(
            Oauth2ProviderType providerType,
            String providerId
    );

    boolean existsByUserIdAndProviderType(Long userId, Oauth2ProviderType providerType);

}
