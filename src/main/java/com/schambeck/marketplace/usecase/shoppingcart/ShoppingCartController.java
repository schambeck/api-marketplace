package com.schambeck.marketplace.usecase.shoppingcart;

import com.schambeck.marketplace.usecase.cartitem.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("shopping-cart")
@RequiredArgsConstructor
public class ShoppingCartController {
    private static final CartItemMapper mapper = CartItemMapper.INSTANCE;
    private final CartItemService service;
    private final CreateShoppingCartItemService createService;
    private final UpdateShoppingCartItemService updateService;
    private final RemoveShoppingCartItemService removeService;

    @PostMapping
    public ResponseEntity<CartItemWeb> create(@RequestBody CreateShoppingCartItemRequest request) {
        CartItem created = createService.create(request);
        return ResponseEntity.ok(mapper.toWeb(created));
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

    @GetMapping("{username}")
    public ResponseEntity<Page<CartItemWeb>> searchByCart(@PageableDefault Pageable pageable, @RequestParam(required = false) String search, @PathVariable String username) {
        return ResponseEntity.ok(service.searchByUsername(pageable, search, username).map(mapper::toWeb));
    }

    @GetMapping("{username}/count")
    public ResponseEntity<CountShoppingCartItemResponse> countByUsername(@PathVariable String username) {
        return ResponseEntity.ok(new CountShoppingCartItemResponse(service.countByUsername(username)));
    }
}
