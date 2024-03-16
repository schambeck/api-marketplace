package com.schambeck.marketplace.usecase.product;

import io.minio.*;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${minio.access.key}")
    private String accessKey;

    @Value("${minio.secret.key}")
    private String secretKey;

    @Value("${minio.bucket.name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.port}")
    private Integer port;

    @Value("${minio.secure}")
    private Boolean secure;

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
                .endpoint(endpoint, port, secure)
                .credentials(accessKey, secretKey)
                .build();
    }
    
    public String store(MultipartFile file) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
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
                    .bucket(bucketName)
                    .object(objectId)
                    .filename(fileToUpload.getAbsolutePath())
                    .build());
            log.info("'{}' is successfully uploaded as object '{}' to bucket '{}'", fileToUpload.getAbsolutePath(), fileToUpload.getName(), bucketName);
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
                            .bucket(bucketName)
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
