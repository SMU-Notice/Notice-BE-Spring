package com.example.noticebespring.service.auth.social.token;

import com.example.noticebespring.service.auth.social.SocialProviderFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

// Google 액세스 토큰 발급
@Service
public class GoogleTokenService extends AbstractTokenService{
    public GoogleTokenService(SocialProviderFactory socialProviderFactory, RestClient restClient) {
        super(socialProviderFactory, restClient);
        setProvider("google");
    }
}

