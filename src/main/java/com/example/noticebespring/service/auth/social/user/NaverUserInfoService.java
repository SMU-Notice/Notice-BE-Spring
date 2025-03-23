package com.example.noticebespring.service.auth.social.user;

import com.example.noticebespring.entity.SocialAccount;
import com.example.noticebespring.repository.SocialAccountRepository;
import com.example.noticebespring.repository.UserRepository;
import com.example.noticebespring.service.auth.social.SocialProviderFactory;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class NaverUserInfoService extends AbstractUserInfoService {
    public NaverUserInfoService(UserRepository userRepository, SocialAccountRepository socialAccountRepository, SocialProviderFactory providerFactory, RestClient restClient) {
        super(userRepository, socialAccountRepository, providerFactory,restClient);
        setProvider("naver");
    }

    @Override
    protected SocialAccount.Provider getProviderType() {
        return SocialAccount.Provider.NAVER;
    }

    @Override
    protected String extractProviderId(JsonNode jsonNode) {
        return jsonNode.has("response") ? jsonNode.get("response").get("id").asText() : null;
    }

    @Override
    protected String extractEmail(JsonNode jsonNode) {
        return jsonNode.has("response") && jsonNode.get("response").has("email")
                ? jsonNode.get("response").get("email").asText()
                : null;
    }
}
