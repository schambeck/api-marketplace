package com.schambeck.marketplace.usecase.image;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService service;
    private final ImageMapper mapper;

    @PostMapping
    public ResponseEntity<ImageWeb> create(@RequestBody ImageWeb web) {
        Image created = service.create(mapper.toEntity(web));
        return ResponseEntity.ok(mapper.toWeb(created));
    }

    @PutMapping("{id}")
    public ResponseEntity<ImageWeb> update(@PathVariable int id, @RequestBody ImageWeb web) {
        Image updated = service.update(id, mapper.toEntity(web));
        return ResponseEntity.ok(mapper.toWeb(updated));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> remove(@PathVariable int id) {
        service.remove(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{id}")
    public ResponseEntity<ImageWeb> get(@PathVariable int id) {
        return ResponseEntity.ok(mapper.toWeb(service.findById(id)));
    }

    @GetMapping("{productId}/product")
    public ResponseEntity<List<ImageWeb>> searchByProduct(@PathVariable int productId) {
        return ResponseEntity.ok(service.searchByProduct(productId).stream().map(mapper::toWeb).toList());
    }

    @GetMapping
    public ResponseEntity<Page<ImageWeb>> search(@PageableDefault Pageable pageable, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(service.search(pageable, search).map(mapper::toWeb));
    }
}
