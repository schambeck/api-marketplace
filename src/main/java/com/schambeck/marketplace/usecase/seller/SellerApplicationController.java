package com.schambeck.marketplace.usecase.seller;

import com.schambeck.marketplace.security.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController @RequestMapping("seller-applications") @RequiredArgsConstructor
public class SellerApplicationController {
    private final SellerApplicationService service;
    private final AuthenticationService authentication;

    @GetMapping("mine")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SellerApplicationWeb> mine(Principal principal) {
        return ResponseEntity.ok(service.mine(authentication.getExternalId(principal)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated() and !hasRole('SELLER')")
    public ResponseEntity<SellerApplicationWeb> submit(@RequestBody SellerApplicationRequest request, Principal principal) {
        return ResponseEntity.ok(service.submit(authentication.getExternalId(principal), request));
    }

    @PostMapping("{id}/withdraw")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SellerApplicationWeb> withdraw(@PathVariable Integer id, Principal principal) {
        return ResponseEntity.ok(service.withdraw(id, authentication.getExternalId(principal)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<SellerApplicationWeb> pending(@PageableDefault(size = 25) Pageable pageable) {
        return service.pending(pageable);
    }

    @PostMapping("{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SellerApplicationWeb> approve(@PathVariable Integer id, @RequestBody SellerApplicationDecision decision, Principal principal) {
        return ResponseEntity.ok(service.decide(id, true, decision.getReason(), authentication.getLoggedUsername(principal)));
    }

    @PostMapping("{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SellerApplicationWeb> reject(@PathVariable Integer id, @RequestBody SellerApplicationDecision decision, Principal principal) {
        return ResponseEntity.ok(service.decide(id, false, decision.getReason(), authentication.getLoggedUsername(principal)));
    }

    @PostMapping("{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SellerApplicationWeb> suspend(@PathVariable Integer id, @RequestBody SellerApplicationDecision decision, Principal principal) {
        return ResponseEntity.ok(service.setSuspended(id, true, decision.getReason(), authentication.getLoggedUsername(principal)));
    }

    @PostMapping("{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SellerApplicationWeb> reactivate(@PathVariable Integer id, @RequestBody SellerApplicationDecision decision, Principal principal) {
        return ResponseEntity.ok(service.setSuspended(id, false, decision.getReason(), authentication.getLoggedUsername(principal)));
    }
}
