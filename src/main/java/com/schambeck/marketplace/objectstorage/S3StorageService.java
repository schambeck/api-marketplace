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
@ConditionalOnExpression("${object-storage.s3.enabled:false}")
public class S3StorageService extends StorageService {
    private final S3StorageConfigProperties config;
    private final MinioClient s3Client;

    @Override
    protected String getBucketName() {
        return config.getBucketName();
    }

    @Override
    protected void createBucket() throws IOException {
        try {
            boolean found = s3Client.bucketExists(BucketExistsArgs.builder().bucket(config.getBucketName()).build());
            if (!found) {
                s3Client.makeBucket(MakeBucketArgs.builder().bucket(config.getBucketName()).build());
            }
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void uploadObject(String objectId, String filePath) throws IOException {
        try {
            s3Client.uploadObject(UploadObjectArgs.builder()
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
            s3Client.downloadObject(DownloadObjectArgs.builder()
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
