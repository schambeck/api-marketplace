package com.schambeck.marketplace.objectstorage;

import com.schambeck.marketplace.usecase.product.StorageService;
import com.schambeck.marketplace.usecase.shoppingcart.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
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
import java.util.List;

@RestController
@RequestMapping("object-storage")
@RequiredArgsConstructor
public class ObjectStorageController {
    private final StorageService storageService;

    @PostMapping("upload-file")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(storageService.store(file));
    }
    
    @PostMapping("upload-multiple-files")
    public ResponseEntity<List<FileUploadResponse>> uploadMultipleFiles(@RequestParam("file[]") MultipartFile[] files) {
        return ResponseEntity.ok(storageService.store(files));
    }

    @GetMapping("download/{objectId}")
    public ResponseEntity<InputStreamResource> downloadImage(@PathVariable String objectId) throws IOException {
        Path path = storageService.get(objectId);
        String contentType = Files.probeContentType(path);
        MimeType mimeType = MimeTypeUtils.parseMimeType(contentType);
        InputStream in = new FileInputStream(path.toAbsolutePath().toString());
        return ResponseEntity.ok().contentType(new MediaType(mimeType)).body(new InputStreamResource(in));
    }
}
