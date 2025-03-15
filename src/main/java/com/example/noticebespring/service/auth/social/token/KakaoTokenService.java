package com.example.noticebespring.service.auth.social.token;

import com.example.noticebespring.service.auth.social.SocialProviderFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


@Service
public class KakaoTokenService extends AbstractTokenService {
    public KakaoTokenService(SocialProviderFactory socialProviderFactory, RestClient restClient) {
        super(socialProviderFactory, restClient);
        setProvider("kakao");
    }
}
