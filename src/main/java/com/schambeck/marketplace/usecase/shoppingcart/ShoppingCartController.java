package com.schambeck.marketplace.usecase.shoppingcart;

import com.schambeck.marketplace.security.AuthenticationService;
import com.schambeck.marketplace.usecase.cartitem.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("shopping-cart")
@RequiredArgsConstructor
public class ShoppingCartController {
    private static final CartItemMapper mapper = CartItemMapper.INSTANCE;
    private final CartItemService service;
    private final CreateShoppingCartItemService createService;
    private final UpdateShoppingCartItemService updateService;
    private final RemoveShoppingCartItemService removeService;
    private final AuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<CartItemWeb> create(@RequestBody CreateShoppingCartItemRequest request, Principal principal) {
        String username = authenticationService.getLoggedUsername(principal);
        CartItem created = createService.create(request, username);
        return ResponseEntity.ok(mapper.toWeb(created));
    }

    @PostMapping("createIfAbsent")
    public ResponseEntity<CreateIfAbsentShoppingCartItemResponse> createIfAbsent(@RequestBody CreateShoppingCartItemRequest request, Principal principal) {
        String username = authenticationService.getLoggedUsername(principal);
        return createService.createIfAbsent(request, username)
                .map(created -> ResponseEntity.ok(new CreateIfAbsentShoppingCartItemResponse(true, mapper.toWeb(created))))
                .orElse(ResponseEntity.ok(new CreateIfAbsentShoppingCartItemResponse(false, null)));
    }

    @PutMapping("{id}")
    public ResponseEntity<CartItemWeb> update(@PathVariable int id, @RequestBody UpdateShoppingCartItemRequest request) {
        CartItem updated = updateService.updateQuantity(id, request);
        return ResponseEntity.ok(mapper.toWeb(updated));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> remove(@PathVariable int id) {
        removeService.remove(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<CartItemWeb>> searchByCart(@PageableDefault Pageable pageable, @RequestParam(required = false) String search, Principal principal) {
        String username = authenticationService.getLoggedUsername(principal);
        return ResponseEntity.ok(service.searchByUsername(pageable, search, username).map(mapper::toWeb));
    }

    @GetMapping("count")
    public ResponseEntity<CountShoppingCartItemResponse> countByUsername(Principal principal) {
        String username = authenticationService.getLoggedUsername(principal);
        return ResponseEntity.ok(new CountShoppingCartItemResponse(service.countByUsername(username)));
    }
}
