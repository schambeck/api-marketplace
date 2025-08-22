package com.schambeck.marketplace.usecase.oauthtoken;

import com.schambeck.marketplace.exception.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Integer> {
    default OAuthToken findByIdOrThrow(Integer id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException(OAuthToken.class, id));
    }

    Optional<OAuthToken> findFirstByUser_IdOrderByIdDesc(Integer userId);
}
