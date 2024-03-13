package com.schambeck.marketplace.usecase.product;

import io.minio.*;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class StorageService {
    public static final String ACCESS_KEY = "";
    public static final String SECRET_KEY = "";
    public static final String BUCKET_NAME = "marketplace";
    private Path rootLocation;
    private MinioClient minioClient;
    
    @PostConstruct
    public void init() {
        try {
            rootLocation = Files.createTempDirectory("minio");
            rootLocation.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        minioClient = MinioClient.builder()
                .endpoint("127.0.0.1", 9010, false)
                .credentials(ACCESS_KEY, SECRET_KEY)
                .build();
    }
    
    public String store(MultipartFile file) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
            } else {
                log.info("Bucket 'marketplace' already exists.");
            }
            
            Path destinationFile = rootLocation.resolve(Paths.get(Objects.requireNonNull(file.getOriginalFilename())))
                    .normalize()
                    .toAbsolutePath();
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            
            File fileToUpload = destinationFile.toFile();
            fileToUpload.deleteOnExit();
            
            String objectId = UUID.randomUUID() + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
            ObjectWriteResponse response = minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(objectId)
                    .filename(fileToUpload.getAbsolutePath())
                    .build());
            log.info("'{}' is successfully uploaded as object '{}' to bucket '{}'", fileToUpload.getAbsolutePath(), fileToUpload.getName(), BUCKET_NAME);
            return response.object();
        } catch (MinioException e) {
            log.error("Error occurred: ", e);
            log.error("HTTP trace: {}", e.httpTrace());
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    
    public Path get(String objectId) {
        try {
            Path file = Files.createTempFile("minio", objectId.substring(objectId.lastIndexOf('.')));
            File storageFile = file.toFile();
            storageFile.deleteOnExit();
            String absolutePath = storageFile.getAbsolutePath();
            minioClient.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(objectId)
                            .filename(absolutePath)
                            .overwrite(true)
                            .build());
            log.info("{} is successfully downloaded to {}", objectId, absolutePath);
            return file;
        } catch (MinioException e) {
            log.error("Error occurred: ", e);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
