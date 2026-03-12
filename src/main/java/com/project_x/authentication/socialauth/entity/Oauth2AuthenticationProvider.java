package com.project_x.authentication.socialauth.entity;

import com.project_x.authentication.socialauth.enums.Oauth2ProviderType;
import com.project_x.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "oauth2_authentication_providers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_provider_type_provider_id", columnNames = {"provider_type", "provider_id"})
        }
)
public class Oauth2AuthenticationProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false)
    private Oauth2ProviderType providerType;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "provider_email")
    private String providerEmail;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
