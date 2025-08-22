package com.schambeck.marketplace.usecase.product;

import com.schambeck.marketplace.usecase.shoppingcart.FileUploadResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public abstract class StorageService {
    private static final String TEMP_FILE_PREFIX = "obj-storage";
    private Path rootLocation;

    @PostConstruct
    private void init() {
        try {
            rootLocation = Files.createTempDirectory(TEMP_FILE_PREFIX);
            rootLocation.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected abstract String getBucketName();
    
    protected abstract void createBucket() throws IOException;
    
    protected abstract void uploadObject(String objectId, String filePath) throws IOException;
    
    protected abstract void downloadObject(String objectId, String destFilePath) throws IOException;
    
    public FileUploadResponse store(MultipartFile file) {
        try {
            createBucket();
            return storeImpl(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public List<FileUploadResponse> store(MultipartFile[] files) {
        try {
            createBucket();
            return Arrays.stream(files).map(this::storeImpl).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private FileUploadResponse storeImpl(MultipartFile file) {
        try {
            Path destinationFile = rootLocation.resolve(Paths.get(Objects.requireNonNull(file.getOriginalFilename())))
                    .normalize()
                    .toAbsolutePath();
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            
            File fileToUpload = destinationFile.toFile();
            fileToUpload.deleteOnExit();
            String filePath = fileToUpload.getAbsolutePath();
            String objectId = UUID.randomUUID() + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
            uploadObject(objectId, filePath);
            log.info("File {} uploaded to bucket {} as {}", filePath, getBucketName(), objectId);
            return new FileUploadResponse(objectId, file.getOriginalFilename(), file.getContentType(), file.getSize());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Path get(String objectId) {
        try {
            String suffix = objectId.substring(objectId.lastIndexOf('.'));
            Path file = Files.createTempFile(TEMP_FILE_PREFIX, suffix);
            File storageFile = file.toFile();
            storageFile.deleteOnExit();
            String destFilePath = storageFile.getAbsolutePath();
            downloadObject(objectId, destFilePath);
            log.debug("Downloaded object {} from bucket name {} to {}", objectId, getBucketName(), destFilePath);
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
