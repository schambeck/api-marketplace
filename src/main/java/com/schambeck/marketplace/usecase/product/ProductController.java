package com.schambeck.marketplace.usecase.product;

import com.schambeck.marketplace.security.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("products")
@RequiredArgsConstructor
public class ProductController {
    private static final ProductMapper mapper = ProductMapper.INSTANCE;
    private final ProductService service;
    private final AuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<ProductWeb> create(@RequestBody ProductWeb web, Principal principal) {
        String username = authenticationService.getLoggedUsername(principal);
        Product created = service.create(mapper.toEntity(web), username);
        return ResponseEntity.ok(mapper.toWeb(created));
    }

    @PutMapping("{id}")
    public ResponseEntity<ProductWeb> update(@PathVariable int id, @RequestBody ProductWeb web) {
        Product updated = service.update(id, mapper.toEntity(web));
        return ResponseEntity.ok(mapper.toWeb(updated));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> remove(@PathVariable int id) {
        service.remove(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{id}")
    public ResponseEntity<ProductWeb> get(@PathVariable int id) {
        return ResponseEntity.ok(mapper.toWeb(service.findById(id)));
    }

    @GetMapping
    public ResponseEntity<Page<ProductWeb>> search(@PageableDefault Pageable pageable, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(service.search(pageable, search).map(mapper::toWeb));
    }
    
    @GetMapping("user")
    public ResponseEntity<Page<ProductWeb>> searchByUser(@PageableDefault Pageable pageable, @RequestParam(required = false) String search, Principal principal) {
        String username = authenticationService.getLoggedUsername(principal);
        return ResponseEntity.ok(service.searchByUser(pageable, search, username).map(mapper::toWeb));
    }
}
