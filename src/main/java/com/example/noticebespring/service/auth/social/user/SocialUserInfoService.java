package com.example.noticebespring.service.auth.social.user;

import com.example.noticebespring.entity.User;

public interface SocialUserInfoService {
    public User processUser(String accessToken);
}
