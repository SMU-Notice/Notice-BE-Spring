package com.example.noticebespring.service.auth.social.user;

import com.example.noticebespring.entity.SocialAccount;
import com.example.noticebespring.repository.SocialAccountRepository;
import com.example.noticebespring.repository.UserRepository;
import com.example.noticebespring.service.auth.social.SocialProviderFactory;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class KakaoUserInfoService extends AbstractUserInfoService {
    public KakaoUserInfoService(UserRepository userRepository, SocialAccountRepository socialAccountRepository, SocialProviderFactory providerFactory, RestClient restClient) {
        super(userRepository, socialAccountRepository, providerFactory, restClient);
        setProvider("kakao");
    }

    @Override
    protected SocialAccount.Provider getProviderType() {
        return SocialAccount.Provider.KAKAO;
    }

    @Override
    protected String extractProviderId(JsonNode jsonNode) {
        return jsonNode.get("id").asText();
    }

    @Override
    protected String extractEmail(JsonNode jsonNode) {
        return jsonNode.has("kakao_account") && jsonNode.get("kakao_account").has("email")
                ? jsonNode.get("kakao_account").get("email").asText()
                : null;
    }
}
