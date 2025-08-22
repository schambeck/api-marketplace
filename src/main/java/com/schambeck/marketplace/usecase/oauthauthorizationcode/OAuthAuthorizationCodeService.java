package com.schambeck.marketplace.usecase.oauthauthorizationcode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthAuthorizationCodeService {
    private final OAuthAuthorizationCodeRepository repository;

    public OAuthAuthorizationCode create(OAuthAuthorizationCode entity) {
        return repository.save(entity);
    }
}
