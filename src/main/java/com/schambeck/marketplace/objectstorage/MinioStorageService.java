package com.schambeck.marketplace.objectstorage;

import com.schambeck.marketplace.usecase.product.StorageService;
import io.minio.*;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("${object-storage.minio.enabled}")
public class MinioStorageService extends StorageService {
    private final MinioStorageConfigProperties config;
    private final MinioClient minioClient;

    @Override
    protected String getBucketName() {
        return config.getBucketName();
    }
    
    @Override
    protected void createBucket() throws IOException {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(config.getBucketName()).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(config.getBucketName()).build());
            } else {
                log.info("Bucket {} already exists", config.getBucketName());
            }
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected void uploadObject(String objectId, String filePath) throws IOException {
        try {
            minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket(config.getBucketName())
                    .object(objectId)
                    .filename(filePath)
                    .build());
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected void downloadObject(String objectId, String destFilePath) throws IOException {
        try {
            minioClient.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket(config.getBucketName())
                            .object(objectId)
                            .filename(destFilePath)
                            .overwrite(true)
                            .build());
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
