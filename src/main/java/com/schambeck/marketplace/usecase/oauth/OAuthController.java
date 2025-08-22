package com.schambeck.marketplace.usecase.oauth;

import com.schambeck.marketplace.security.AuthenticationService;
import com.schambeck.marketplace.usecase.oauthauthorizationcode.OAuthAuthorizationCode;
import com.schambeck.marketplace.usecase.oauthauthorizationrequest.OAuthAuthorizationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.security.Principal;
import java.util.Map;

import static org.springframework.http.HttpStatus.MOVED_PERMANENTLY;

@Slf4j
@RestController
@RequestMapping("oauth")
@RequiredArgsConstructor
public class OAuthController {
    private final OAuthService service;
    private final AuthenticationService authenticationService;
    
    @Value("${custom.frontend-url}")
    private String frontendUrl;
    @Value("${mercado-pago.client-id}")
    private String clientId;
    @Value("${mercado-pago.redirect-uri}")
    private String redirectUri;
    private String state;
    
    @GetMapping("start-flow")
    public ResponseEntity<Map<String, String>> startFlow(Principal principal) {
        String loggedUsername = authenticationService.getLoggedUsername(principal);
        log.info("Starting flow for {}", loggedUsername);
        OAuthAuthorizationRequest request = service.startFlow(principal.getName());
        state = request.getState();
        log.info("Started flow for {} with state {}", loggedUsername, state);
        String url = buildAuthorizationUrl();
        log.info("Redirecting to {} with state {}", url, state);
        return ResponseEntity.ok(Map.of("redirectUrl", url));
    }
    
    @GetMapping
    public ResponseEntity<Void> oauth(@RequestParam String code) {
        log.info("Auth step for state {}", state);
        OAuthAuthorizationRequest requestEntity = service.findByState(state);
        log.info("Found request for {} with state {}", requestEntity.getUser().getUsername(), state);
        OAuthAuthorizationCode codeEntity = service.createCode(code, requestEntity.getUser(), requestEntity.getClientId(), requestEntity.getRedirectUri());
        service.generateToken(code, requestEntity.getState(), codeEntity.getUser());
        HttpHeaders headers = createHeaderLocation(createUserUrl(requestEntity.getUser().getId()));
        return new ResponseEntity<>(headers, MOVED_PERMANENTLY);
    }
    
    private HttpHeaders createHeaderLocation(String location) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(location));
        return headers;
    }
    
    private String buildAuthorizationUrl() {
        return "https://auth.mercadopago.com.br/authorization?" +
                "client_id=" + clientId +
                "&response_type=code" +
                "&platform_id=mp" +
                "&redirect_uri=" + redirectUri;
    }
    
    private String createUserUrl(Integer id) {
        return frontendUrl + "/user/" + id + "?tokenGenerated=true";
    }
    
//    private GenerateTokenMercadoPagoRequest createRequest(String code) {
//        return GenerateTokenMercadoPagoRequest.builder()
//                .clientId(clientId)
//                .clientSecret(clientSecret)
//                .grantType(AUTHORIZATION_CODE)
//                .code(code)
//                .redirectUri(redirectUri)
//                .state(UUID.randomUUID().toString())
//                .build();
//    }
}
