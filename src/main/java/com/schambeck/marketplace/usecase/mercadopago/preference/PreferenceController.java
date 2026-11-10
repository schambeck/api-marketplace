package com.schambeck.marketplace.usecase.mercadopago.preference;

import com.mercadopago.resources.preference.Preference;
import com.schambeck.marketplace.security.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("mercado-pago/preferences")
@RequiredArgsConstructor
class PreferenceController {
    private final AuthenticationService authenticationService;
    private final CreatePreferenceService createService;
    
    @PostMapping
    ResponseEntity<Preference> createPreference(@RequestHeader(value = "Idempotency-Key", required = false) String key, Principal principal) {
        String username = authenticationService.getLoggedUsername(principal);
        Preference preference = createService.createPreference(username, key == null ? java.util.UUID.randomUUID().toString() : key);
        return ResponseEntity.ok(preference);
    }

    @PostMapping("/orders/{publicId}")
    ResponseEntity<Preference> createOrderPreference(@PathVariable UUID publicId,
                                                     @RequestHeader(value = "Idempotency-Key", required = false) String key,
                                                     Principal principal) {
        String username = authenticationService.getLoggedUsername(principal);
        Preference preference = createService.createPreferenceForOrder(publicId, username,
                key == null ? "order:" + publicId + ":preference" : key);
        return ResponseEntity.ok(preference);
    }
}
