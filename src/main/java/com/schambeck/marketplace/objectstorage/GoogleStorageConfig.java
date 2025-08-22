package com.schambeck.marketplace.objectstorage;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Configuration
public class GoogleStorageConfig {
    @Bean
    @ConditionalOnExpression("${object-storage.gcs.enabled}")
    public Storage storage(GoogleStorageConfigProperties config) throws IOException {
        try (InputStream credentialsFile = getClass().getResourceAsStream(config.getCredentialsFile())) {
            Credentials credentials = GoogleCredentials.fromStream(Objects.requireNonNull(credentialsFile));
            return StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .setProjectId(config.getProjectId())
                    .build()
                    .getService();
        }
    }
}
