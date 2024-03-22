package com.schambeck.marketplace.objectstorage;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class GoogleStorageConfig {
    @Bean
    public Storage storage(GoogleStorageConfigProperties config) throws IOException {
        InputStream credentialsFile = new FileInputStream(config.getCredentialsFile());
        Credentials credentials = GoogleCredentials.fromStream(credentialsFile);
        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .setProjectId(config.getProjectId())
                .build()
                .getService();
    }
}
