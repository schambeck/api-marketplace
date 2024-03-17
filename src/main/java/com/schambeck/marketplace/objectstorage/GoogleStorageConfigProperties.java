package com.schambeck.marketplace.objectstorage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "object-storage.gcs")
public class GoogleStorageConfigProperties {
    private String projectId;
    private String bucketName;
 }
