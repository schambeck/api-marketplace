package com.schambeck.marketplace.usecase.offer;

import com.schambeck.marketplace.security.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Locale;

@RestController
@RequestMapping("seller/offers")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SellerOfferController {
    private final OfferService service;
    private final AuthenticationService authenticationService;

    @GetMapping
    public ResponseEntity<Page<SellerOfferWeb>> search(
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Principal principal) {
        OfferStatus parsedStatus = parseStatus(status);
        return ResponseEntity.ok(service.searchOwned(username(principal), pageable, parsedStatus, search)
                .map(OfferMapper::toSellerWeb));
    }

    @GetMapping("{id}")
    public ResponseEntity<SellerOfferWeb> get(@PathVariable Integer id, Principal principal) {
        return ResponseEntity.ok(OfferMapper.toSellerWeb(service.findOwnedById(username(principal), id)));
    }

    private String username(Principal principal) {
        return authenticationService.getLoggedUsername(principal);
    }

    private OfferStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return OfferStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("status must be one of DRAFT, PUBLISHED, PAUSED or RETIRED.", exception);
        }
    }
}
