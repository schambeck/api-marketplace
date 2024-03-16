package com.schambeck.marketplace.usecase.mercadopago.preference;

import com.mercadopago.resources.preference.Preference;
import com.schambeck.marketplace.security.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("mercado-pago/preferences")
@RequiredArgsConstructor
class PreferenceController {
    private final AuthenticationService authenticationService;
    private final CreatePreferenceService createService;
    
    @PostMapping
    ResponseEntity<Preference> createPreference(Principal principal, HttpServletRequest request) {
        String username = authenticationService.getLoggedUsername(principal);
        request.getRequestURL().toString();
        Preference preference = createService.createPreference(username);
        return ResponseEntity.ok(preference);
    }
}
