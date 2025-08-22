package com.schambeck.marketplace.usecase.oauthauthorizationrequest;

import com.schambeck.marketplace.exception.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OAuthAuthorizationRequestRepository extends JpaRepository<OAuthAuthorizationRequest, Integer> {
    default OAuthAuthorizationRequest findByIdOrThrow(Integer id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException(OAuthAuthorizationRequest.class, id));
    }

    default OAuthAuthorizationRequest findByStateOrThrow(String state) {
        return findByState(state).orElseThrow(() -> new EntityNotFoundException(OAuthAuthorizationRequest.class, state));
    }
    
    Optional<OAuthAuthorizationRequest> findByState(String state);
}
