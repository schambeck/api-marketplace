package com.schambeck.marketplace.usecase.inventory;

import com.schambeck.marketplace.security.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("inventory")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class InventoryController {
    private final InventoryService service;
    private final AuthenticationService authenticationService;

    @PostMapping("reservations")
    public ResponseEntity<InventoryReservationWeb> reserve(@RequestBody InventoryReservationRequest request, Principal principal) {
        return ResponseEntity.ok(InventoryReservationWeb.from(service.reserve(request.getProductId(), request.getOrderItemId(),
                request.getQuantity(), request.getOperationKey(), username(principal))));
    }

    @PostMapping("reservations/{id}/allocate")
    public ResponseEntity<InventoryAllocationWeb> allocate(@PathVariable Integer id,
                                                            @RequestBody InventoryAllocationRequest request,
                                                            Principal principal, Authentication authentication) {
        return ResponseEntity.ok(InventoryAllocationWeb.from(service.allocate(id, request.getQuantity(),
                request.getOperationKey(), username(principal), isAdmin(authentication))));
    }

    @PostMapping("reservations/{id}/release")
    public ResponseEntity<InventoryReservationWeb> release(@PathVariable Integer id, @RequestParam String operationKey,
                                                            Principal principal, Authentication authentication) {
        return ResponseEntity.ok(InventoryReservationWeb.from(service.release(id, operationKey, username(principal), isAdmin(authentication))));
    }

    @PostMapping("reservations/{id}/expire")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventoryReservationWeb> expire(@PathVariable Integer id, @RequestParam String operationKey,
                                                           Principal principal) {
        return ResponseEntity.ok(InventoryReservationWeb.from(service.expire(id, operationKey, username(principal))));
    }

    @PostMapping("allocations/{id}/consume")
    public ResponseEntity<InventoryAllocationWeb> consume(@PathVariable Integer id,
                                                           @RequestBody InventoryConsumptionRequest request,
                                                           Principal principal, Authentication authentication) {
        return ResponseEntity.ok(InventoryAllocationWeb.from(service.consumeDelivered(id, request.getQuantity(),
                request.getOperationKey(), username(principal), isAdmin(authentication))));
    }

    @PostMapping("reservations/{id}/consume")
    public ResponseEntity<InventoryAllocationWeb> consumeReservation(@PathVariable Integer id,
                                                                       @RequestBody InventoryConsumptionRequest request,
                                                                       Principal principal, Authentication authentication) {
        return consumeAllocationForReservation(id, request, principal, authentication);
    }

    private ResponseEntity<InventoryAllocationWeb> consumeAllocationForReservation(Integer reservationId,
                                                                                      InventoryConsumptionRequest request,
                                                                                      Principal principal,
                                                                                      Authentication authentication) {
        return ResponseEntity.ok(InventoryAllocationWeb.from(service.consumeReservation(reservationId, request.getQuantity(),
                request.getOperationKey(), username(principal), isAdmin(authentication))));
    }

    private String username(Principal principal) {
        return authenticationService.getLoggedUsername(principal);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
