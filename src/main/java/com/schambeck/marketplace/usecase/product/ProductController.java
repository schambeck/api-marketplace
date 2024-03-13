package com.schambeck.marketplace.usecase.product;

import com.schambeck.marketplace.security.AuthenticationService;
import com.schambeck.marketplace.usecase.shoppingcart.FileUploadResponse;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;

@RestController
@RequestMapping("products")
@RequiredArgsConstructor
public class ProductController {
    private static final ProductMapper mapper = ProductMapper.INSTANCE;
    private final ProductService service;
    private final AuthenticationService authenticationService;
    private final StorageService storageService;

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
    
    @PostMapping("upload")
    public ResponseEntity<FileUploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        String objectId = storageService.store(file);
        return ResponseEntity.ok(new FileUploadResponse(objectId));
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
