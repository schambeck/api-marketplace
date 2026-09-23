package com.schambeck.marketplace.usecase.offer;

import com.schambeck.marketplace.security.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("offers")
@RequiredArgsConstructor
public class OfferController {
    private final OfferService service;
    private final AuthenticationService authenticationService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OfferWeb> create(@RequestBody OfferWeb web, Principal principal, Authentication authentication) {
        return ResponseEntity.ok(OfferMapper.toWeb(service.create(web, username(principal), isAdmin(authentication))));
    }

    @PutMapping("{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OfferWeb> updateDraft(@PathVariable Integer id, @RequestBody OfferUpdateRequest request,
                                                 Principal principal, Authentication authentication) {
        return ResponseEntity.ok(OfferMapper.toWeb(service.updateDraft(id, request, username(principal), isAdmin(authentication))));
    }

    @GetMapping
    public ResponseEntity<Page<OfferWeb>> search(@PageableDefault Pageable pageable,
                                                 @RequestParam(required = false) String search) {
        return ResponseEntity.ok(service.searchPublished(pageable, search).map(OfferMapper::toWeb));
    }

    @GetMapping("{id}")
    public ResponseEntity<OfferWeb> get(@PathVariable Integer id) {
        return ResponseEntity.ok(OfferMapper.toWeb(service.findPublishedById(id)));
    }

    @GetMapping("{id}/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OfferStatusHistory>> history(@PathVariable Integer id, Principal principal, Authentication authentication) {
        return ResponseEntity.ok(service.history(id, username(principal), isAdmin(authentication)));
    }

    @PostMapping("{id}/publish")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OfferWeb> publish(@PathVariable Integer id, @RequestBody OfferActionRequest request, Principal principal, Authentication authentication) {
        return ResponseEntity.ok(OfferMapper.toWeb(service.publish(id, request, username(principal), isAdmin(authentication))));
    }

    @PostMapping("{id}/pause")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OfferWeb> pause(@PathVariable Integer id, @RequestBody OfferActionRequest request, Principal principal, Authentication authentication) {
        return ResponseEntity.ok(OfferMapper.toWeb(service.pause(id, request, username(principal), isAdmin(authentication))));
    }

    @PostMapping("{id}/reactivate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OfferWeb> reactivate(@PathVariable Integer id, @RequestBody OfferActionRequest request, Principal principal, Authentication authentication) {
        return ResponseEntity.ok(OfferMapper.toWeb(service.reactivate(id, request, username(principal), isAdmin(authentication))));
    }

    @PostMapping("{id}/retire")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OfferWeb> retire(@PathVariable Integer id, @RequestBody OfferActionRequest request, Principal principal, Authentication authentication) {
        return ResponseEntity.ok(OfferMapper.toWeb(service.retire(id, request, username(principal), isAdmin(authentication))));
    }

    @PostMapping("{id}/availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OfferWeb> availability(@PathVariable Integer id, @RequestBody OfferAvailabilityRequest request, Principal principal, Authentication authentication) {
        return ResponseEntity.ok(OfferMapper.toWeb(service.changeAvailability(id, request, username(principal), isAdmin(authentication))));
    }

    private String username(Principal principal) {
        return authenticationService.getLoggedUsername(principal);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
