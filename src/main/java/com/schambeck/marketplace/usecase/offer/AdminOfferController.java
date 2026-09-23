package com.schambeck.marketplace.usecase.offer;

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

import java.util.Locale;

@RestController
@RequestMapping("admin/offers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOfferController {
    private final OfferService service;

    @GetMapping
    public ResponseEntity<Page<OfferWeb>> search(
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(service.searchAdmin(pageable, parseStatus(status), search)
                .map(OfferMapper::toWeb));
    }

    @GetMapping("{id}")
    public ResponseEntity<OfferWeb> get(@PathVariable Integer id) {
        return ResponseEntity.ok(OfferMapper.toWeb(service.findAdminById(id)));
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
