package com.project_x.authentication.socialauth.service;

import com.project_x.authentication.socialauth.enums.Oauth2ProviderType;

public interface SocialUserInfo {
    Oauth2ProviderType getProviderType();

    String getProviderId();

    String getEmail();

    boolean isEmailVerified();

    String getFirstName();

    String getLastName();

    String getDisplayName();
}
