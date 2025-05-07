package com.example.noticebespring.service.auth.social.token;

import com.example.noticebespring.service.auth.social.SocialProviderFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class NaverTokenService extends AbstractTokenService{
    public NaverTokenService(SocialProviderFactory socialProviderFactory, RestClient restClient) {
        super(socialProviderFactory, restClient);
        setProvider("naver");
    }

    @Override
    public String getToken(String code, String state) { // 네이버는 state 필수
        return super.getToken(code, state);
    }
}
