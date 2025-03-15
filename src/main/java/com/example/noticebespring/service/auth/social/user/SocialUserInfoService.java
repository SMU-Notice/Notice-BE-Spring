package com.example.noticebespring.service.auth.social.user;

import com.example.noticebespring.domain.User;

public interface SocialUserInfoService {
    public User processUser(String accessToken);
}
