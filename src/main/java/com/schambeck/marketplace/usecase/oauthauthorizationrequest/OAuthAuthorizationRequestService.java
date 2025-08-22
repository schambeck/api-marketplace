package com.schambeck.marketplace.usecase.oauthauthorizationrequest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthAuthorizationRequestService {
    private final OAuthAuthorizationRequestRepository repository;

    public OAuthAuthorizationRequest create(OAuthAuthorizationRequest entity) {
        return repository.save(entity);
    }
    
    public OAuthAuthorizationRequest findByState(String state) {
        return repository.findByStateOrThrow(state);
    }
}
