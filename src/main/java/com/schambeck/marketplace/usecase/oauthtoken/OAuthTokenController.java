
package com.schambeck.marketplace.usecase.oauthtoken;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("oauth-tokens")
@RequiredArgsConstructor
public class OAuthTokenController {
    private final OAuthTokenService service;
    
    @GetMapping("user/{userId}")
    public ResponseEntity<OAuthToken> findFirstByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(service.findFirstByUserId(userId));
    }
}
