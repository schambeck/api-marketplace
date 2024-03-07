package com.schambeck.marketplace.usecase.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("carts")
@RequiredArgsConstructor
public class CartController {
    private static final CartMapper mapper = CartMapper.INSTANCE;
    private final CartService service;

    @PostMapping
    public ResponseEntity<CartWeb> create(@RequestBody CartWeb web) {
        Cart created = service.create(mapper.toEntity(web));
        return ResponseEntity.ok(mapper.toWeb(created));
    }

    @PutMapping("{id}")
    public ResponseEntity<CartWeb> update(@PathVariable int id, @RequestBody CartWeb web) {
        Cart updated = service.update(id, mapper.toEntity(web));
        return ResponseEntity.ok(mapper.toWeb(updated));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> remove(@PathVariable int id) {
        service.remove(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{username}")
    public ResponseEntity<CartWeb> get(@PathVariable String username) {
        return ResponseEntity.ok(mapper.toWeb(service.findByUsername(username)));
    }

    @GetMapping
    public ResponseEntity<Page<CartWeb>> search(@PageableDefault Pageable pageable, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(service.search(pageable, search).map(mapper::toWeb));
    }
}
