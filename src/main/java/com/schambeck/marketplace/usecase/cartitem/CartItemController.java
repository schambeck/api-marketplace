package com.schambeck.marketplace.usecase.cartitem;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("cart-items")
@RequiredArgsConstructor
public class CartItemController {
    private static final CartItemMapper mapper = CartItemMapper.INSTANCE;
    private final CartItemService service;

    @PostMapping
    public ResponseEntity<CartItemWeb> create(@RequestBody CartItemWeb web) {
        CartItem created = service.create(mapper.toEntity(web));
        return ResponseEntity.ok(mapper.toWeb(created));
    }

    @PutMapping("{id}")
    public ResponseEntity<CartItemWeb> update(@PathVariable int id, @RequestBody CartItemWeb web) {
        CartItem updated = service.update(id, mapper.toEntity(web));
        return ResponseEntity.ok(mapper.toWeb(updated));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> remove(@PathVariable int id) {
        service.remove(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{id}")
    public ResponseEntity<CartItemWeb> get(@PathVariable int id) {
        return ResponseEntity.ok(mapper.toWeb(service.findById(id)));
    }

    @GetMapping
    public ResponseEntity<Page<CartItemWeb>> search(@PageableDefault Pageable pageable, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(service.searchByCart(pageable, search).map(mapper::toWeb));
    }
}
