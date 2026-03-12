package com.project_x.authentication.socialauth.service.impl;


import com.project_x.authentication.socialauth.factory.SocialUserInfoFactory;
import com.project_x.authentication.socialauth.service.SocialAuthenticationService;
import com.project_x.authentication.socialauth.service.SocialUserInfo;
import com.project_x.user.entity.User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOauth2UserServiceImpl implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final SocialAuthenticationService socialAuthenticationService;

    public CustomOauth2UserServiceImpl(SocialAuthenticationService socialAuthenticationService) {
        this.socialAuthenticationService = socialAuthenticationService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        SocialUserInfo socialUserInfo = SocialUserInfoFactory.from(registrationId, oauth2User);

        User user = socialAuthenticationService.handleSocialLogin(socialUserInfo);

        // Return an authenticated principal containing app authorities
        return new DefaultOAuth2User(
                user.getAuthorities(),
                oauth2User.getAttributes(),
                "sub"
        );
    }
}
