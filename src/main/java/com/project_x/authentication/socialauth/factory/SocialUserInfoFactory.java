package com.project_x.authentication.socialauth.factory;

import com.project_x.authentication.socialauth.service.SocialUserInfo;
import com.project_x.authentication.socialauth.service.impl.GoogleSocialUserInfo;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class SocialUserInfoFactory {

    public static SocialUserInfo from(String registrationId, OAuth2User oauth2User) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleSocialUserInfo(oauth2User);

            // write other provider cases here

            default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        };
    }
}
