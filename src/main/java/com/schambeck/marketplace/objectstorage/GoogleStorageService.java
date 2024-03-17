package com.schambeck.marketplace.objectstorage;

import com.google.cloud.storage.*;
import com.schambeck.marketplace.usecase.product.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("${object-storage.gcs.enabled}")
public class GoogleStorageService extends StorageService {
    private final GoogleStorageConfigProperties config;
    private final Storage storage;
    
    @Override
    protected String getBucketName() {
        return config.getBucketName();
    }
    
    @Override
    protected void createBucket() {
        boolean found = storage.get(config.getBucketName()) != null;
        if (!found) {
            storage.create(BucketInfo.of(config.getBucketName()));
        } else {
            log.info("Bucket {} already exists", config.getBucketName());
        }
    }
    
    @Override
    protected void uploadObject(String objectId, String filePath) throws IOException {
        BlobId blobId = BlobId.of(config.getBucketName(), objectId);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        Storage.BlobWriteOption precondition;
        if (storage.get(config.getBucketName(), objectId) == null) {
            precondition = Storage.BlobWriteOption.doesNotExist();
        } else {
            precondition = Storage.BlobWriteOption.generationMatch(storage.get(config.getBucketName(), objectId).getGeneration());
        }
        storage.createFrom(blobInfo, Paths.get(filePath), precondition);
    }
    
    @Override
    protected void downloadObject(String objectId, String destFilePath) {
        Blob blob = storage.get(BlobId.of(config.getBucketName(), objectId));
        blob.downloadTo(Paths.get(destFilePath));
    }
}
