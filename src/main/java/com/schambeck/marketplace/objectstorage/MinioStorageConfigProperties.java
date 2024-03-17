package com.schambeck.marketplace.objectstorage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("object-storage.minio")
public class MinioStorageConfigProperties {
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String endpoint;
    private Integer port;
    private Boolean secure;
 }
