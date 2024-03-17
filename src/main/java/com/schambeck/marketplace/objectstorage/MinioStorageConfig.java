package com.schambeck.marketplace.objectstorage;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioStorageConfig {
    @Bean
    @ConditionalOnExpression("${object-storage.minio.enabled}")
    public MinioClient minioClient(MinioStorageConfigProperties config) {
        return MinioClient.builder()
                .endpoint(config.getEndpoint(), config.getPort(), config.getSecure())
                .credentials(config.getAccessKey(), config.getSecretKey())
                .build();
    }
}
