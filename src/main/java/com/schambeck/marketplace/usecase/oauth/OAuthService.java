package com.schambeck.marketplace.usecase.oauth;

import com.schambeck.marketplace.objectstorage.GenerateTokenMercadoPagoResponse;
import com.schambeck.marketplace.objectstorage.MercadoPagoClient;
import com.schambeck.marketplace.usecase.oauthauthorizationcode.OAuthAuthorizationCode;
import com.schambeck.marketplace.usecase.oauthauthorizationcode.OAuthAuthorizationCodeService;
import com.schambeck.marketplace.usecase.oauthauthorizationrequest.OAuthAuthorizationRequest;
import com.schambeck.marketplace.usecase.oauthauthorizationrequest.OAuthAuthorizationRequestService;
import com.schambeck.marketplace.usecase.oauthtoken.OAuthToken;
import com.schambeck.marketplace.usecase.oauthtoken.OAuthTokenService;
import com.schambeck.marketplace.usecase.user.User;
import com.schambeck.marketplace.usecase.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private final MercadoPagoClient mercadoPagoClient;
    private final OAuthAuthorizationRequestService requestService;
    private final OAuthAuthorizationCodeService codeService;
    private final OAuthTokenService tokenService;
    private final UserService userService;
    
    @Value("${mercado-pago.client-id}")
    private String clientId;
    @Value("${mercado-pago.client-secret}")
    private String clientSecret;
    @Value("${mercado-pago.redirect-uri}")
    private String redirectUri;
    
    OAuthAuthorizationRequest startFlow(String name) {
        return requestService.create(createOAuthEvent(name));
    }
    
    OAuthAuthorizationRequest findByState(String state) {
        return requestService.findByState(state);
    }
    
    void generateToken(String code, String state, User user) {
        GenerateTokenMercadoPagoResponse response = mercadoPagoClient.generateToken(createRequest(code, state));
        OAuthToken token = createToken(user, response.accessToken, response.refreshToken, response.publicKey, response.scope, response.expiresIn);
        log.info("Public Key for {}: {}", user.getUsername(), token.getPublicKey());
        userService.updateUser(user.getId(), token.getPublicKey());
//        log.info("Setting access token holder for {}: {}", user.getUsername(), response.accessToken);
//        AccessTokenHolder.setAccessToken(response.accessToken);
    }
    
    OAuthAuthorizationCode createCode(String code, User user, String clientId, String redirectUri) {
        return codeService.create(OAuthAuthorizationCode.builder()
                .user(user)
                .code(code)
                .clientId(clientId)
                .redirectUri(redirectUri)
                .expiresAt(LocalDateTime.now())
                .used(false)
                .build());
    }
    
    private OAuthAuthorizationRequest createOAuthEvent(String externalId) {
        return requestService.create(OAuthAuthorizationRequest.builder()
                .user(userService.findByExternalIdOrThrow(externalId))
                .clientId(clientId)
                .redirectUri(redirectUri)
                .state(UUID.randomUUID().toString())
                .build());
    }
    
    private OAuthToken createToken(User user, String accessToken, String refreshToken, String publicKey, String scope, Long expiresIn) {
        return tokenService.create(OAuthToken.builder()
                .user(user)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .publicKey(publicKey)
                .clientId(clientId)
                .scope(scope)
                .expiresAt(LocalDateTime.now())
                .build());
    }
    
    Map<String, Object> createRequest(String code, String state) {
        return Map.of("client_id", clientId,
                "client_secret", clientSecret,
                "grant_type", AUTHORIZATION_CODE,
                "code", code,
                "redirect_uri", redirectUri,
                "state", state.toString());
    }
}
