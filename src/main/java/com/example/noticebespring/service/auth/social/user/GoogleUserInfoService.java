package com.example.noticebespring.service.auth.social.user;

import com.example.noticebespring.entity.SocialAccount;
import com.example.noticebespring.repository.SocialAccountRepository;
import com.example.noticebespring.repository.UserRepository;
import com.example.noticebespring.service.auth.social.SocialProviderFactory;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GoogleUserInfoService extends AbstractUserInfoService {

    public GoogleUserInfoService(UserRepository userRepository, SocialAccountRepository socialAccountRepository, SocialProviderFactory providerFactory, RestClient restClient) {
        super(userRepository, socialAccountRepository, providerFactory, restClient);
        setProvider("google");
    }

    @Override
    protected SocialAccount.Provider getProviderType() {
        return SocialAccount.Provider.GOOGLE;
    }

    @Override
    protected String extractProviderId(JsonNode jsonNode) {
        return jsonNode.get("sub").asText();
    }

    @Override
    protected String extractEmail(JsonNode jsonNode) {
        return jsonNode.has("email")?jsonNode.get("email").asText() : null;
    }
}
