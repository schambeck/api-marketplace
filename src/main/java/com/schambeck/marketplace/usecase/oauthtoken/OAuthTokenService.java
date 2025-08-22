package com.schambeck.marketplace.usecase.oauthtoken;

import com.schambeck.marketplace.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthTokenService {
    private final OAuthTokenRepository repository;

    public OAuthToken create(OAuthToken entity) {
        return repository.save(entity);
    }
    
    public OAuthToken findFirstByUserId(Integer userId) {
        return repository.findFirstByUser_IdOrderByIdDesc(userId).orElseThrow(() -> new EntityNotFoundException(OAuthToken.class, userId));
    }
}
