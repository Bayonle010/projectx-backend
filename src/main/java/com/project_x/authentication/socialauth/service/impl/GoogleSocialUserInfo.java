package com.project_x.authentication.socialauth.service.impl;

import com.project_x.authentication.socialauth.enums.Oauth2ProviderType;
import com.project_x.authentication.socialauth.service.SocialUserInfo;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class GoogleSocialUserInfo implements SocialUserInfo {

    private final OAuth2User oAuth2User;

    public GoogleSocialUserInfo(OAuth2User oAuth2User) {
        this.oAuth2User = oAuth2User;
    }

    @Override
    public Oauth2ProviderType getProviderType() {
        return Oauth2ProviderType.GOOGLE;
    }

    @Override
    public String getProviderId() {
        return oAuth2User.getAttribute("sub");
    }

    @Override
    public String getEmail() {
        return oAuth2User.getAttribute("email");
    }

    @Override
    public boolean isEmailVerified() {
        Boolean emailVerified = oAuth2User.getAttribute("email_verified");
        return Boolean.TRUE.equals(emailVerified);
    }

    @Override
    public String getFirstName() {
        return oAuth2User.getAttribute("given_name");
    }

    @Override
    public String getLastName() {
        return oAuth2User.getAttribute("family_name");
    }

    @Override
    public String getDisplayName() {
        return oAuth2User.getAttribute("name");
    }
}
