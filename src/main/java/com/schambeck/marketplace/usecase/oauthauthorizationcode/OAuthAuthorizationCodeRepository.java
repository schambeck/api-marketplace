package com.schambeck.marketplace.usecase.oauthauthorizationcode;

import com.schambeck.marketplace.exception.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthAuthorizationCodeRepository extends JpaRepository<OAuthAuthorizationCode, Integer> {
    default OAuthAuthorizationCode findByIdOrThrow(Integer id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException(OAuthAuthorizationCode.class, id));
    }
}
