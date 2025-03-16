package com.example.noticebespring.service.auth.social.token;



public interface SocialTokenService {
    public String getToken(String code, String state);
}
