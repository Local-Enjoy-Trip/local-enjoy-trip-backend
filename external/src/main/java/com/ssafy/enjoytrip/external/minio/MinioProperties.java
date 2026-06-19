package com.ssafy.enjoytrip.external.minio;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "enjoytrip.minio")
public class MinioProperties {
    private String endpoint = "http://localhost:9000";
    private String bucket = "dongnepin-notes";
    private String accessKey = "minioadmin";
    private String secretKey = "minioadmin";
    private String region = "ap-northeast-2";
    private String publicBaseUrl = "http://localhost:9000/dongnepin-notes";
    private Duration uploadExpiry = Duration.ofMinutes(10);

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public Duration getUploadExpiry() {
        return uploadExpiry;
    }

    public void setUploadExpiry(Duration uploadExpiry) {
        this.uploadExpiry = uploadExpiry;
    }
}
