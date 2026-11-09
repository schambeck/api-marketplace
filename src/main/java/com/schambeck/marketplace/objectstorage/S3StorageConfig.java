package com.schambeck.marketplace.objectstorage;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3StorageConfig {
    @Bean
    @ConditionalOnExpression("${object-storage.s3.enabled:false}")
    public MinioClient s3Client(S3StorageConfigProperties config) {
        return MinioClient.builder()
                .endpoint(config.getEndpoint())
                .credentials(config.getAccessKey(), config.getSecretKey())
                .region(config.getRegion())
                .build();
    }
}
