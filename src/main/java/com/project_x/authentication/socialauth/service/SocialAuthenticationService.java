package com.project_x.authentication.socialauth.service;

import com.project_x.user.entity.User;

public interface SocialAuthenticationService {
    User handleSocialLogin(SocialUserInfo socialUserInfo);
}
