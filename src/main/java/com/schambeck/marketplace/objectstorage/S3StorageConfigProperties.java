package com.schambeck.marketplace.objectstorage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("object-storage.s3")
public class S3StorageConfigProperties {
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String endpoint;
    private String region = "us-east-1";
    private Boolean pathStyleAccess = true;
    private Boolean secure = false;
}
