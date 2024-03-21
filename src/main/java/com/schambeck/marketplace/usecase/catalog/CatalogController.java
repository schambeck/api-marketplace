package com.schambeck.marketplace.usecase.catalog;

import com.schambeck.marketplace.usecase.product.ProductMapper;
import com.schambeck.marketplace.usecase.product.ProductService;
import com.schambeck.marketplace.usecase.product.ProductWeb;
import com.schambeck.marketplace.usecase.product.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("catalog")
@RequiredArgsConstructor
public class CatalogController {
    private static final ProductMapper mapper = ProductMapper.INSTANCE;
    private final ProductService service;
    private final StorageService storageService;

    @GetMapping("{id}")
    public ResponseEntity<ProductWeb> get(@PathVariable int id) {
        return ResponseEntity.ok(mapper.toWeb(service.findById(id)));
    }

    @GetMapping
    public ResponseEntity<Page<ProductWeb>> search(@PageableDefault Pageable pageable, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(service.search(pageable, search).map(mapper::toWeb));
    }
    
    @GetMapping("download/{objectId}")
    public @ResponseBody ResponseEntity<InputStreamResource> downloadImage(@PathVariable String objectId) throws IOException {
        Path path = storageService.get(objectId);
        String contentType = Files.probeContentType(path);
        MimeType mimeType = MimeTypeUtils.parseMimeType(contentType);
        InputStream in = new FileInputStream(path.toAbsolutePath().toString());
        return ResponseEntity.ok().contentType(new MediaType(mimeType)).body(new InputStreamResource(in));
    }
}
