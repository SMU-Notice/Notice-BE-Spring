package com.example.noticebespring.service.auth.social.user;

import com.example.noticebespring.domain.User;

public interface SocialUserService {
    public User processUser(String accessToken);
}
