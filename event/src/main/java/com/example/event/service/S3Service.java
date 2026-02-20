package com.example.event.service;

import java.util.Base64;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;
    private final String bucketName;
    private final String region;

    public S3Service(
            @Value("${aws.s3.bucket-name:#{null}}") String bucketName,
            @Value("${aws.s3.region:eu-west-2}") String region) {
        this.bucketName = bucketName;
        this.region = region;

        if (bucketName != null && !bucketName.isEmpty()) {
            this.s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
            logger.info("S3 service initialized with bucket: {}", bucketName);
        } else {
            this.s3Client = null;
            logger.info("S3 bucket not configured -- images will be stored as base64 in MongoDB (local dev mode)");
        }
    }

    public boolean isEnabled() {
        return s3Client != null && bucketName != null;
    }

    public String uploadImage(String base64Data) {
        if (!isEnabled()) {
            return null;
        }

        try {
            String contentType = "image/jpeg";
            String rawBase64 = base64Data;

            if (base64Data.contains(",")) {
                String header = base64Data.substring(0, base64Data.indexOf(","));
                if (header.contains("png")) {
                    contentType = "image/png";
                } else if (header.contains("gif")) {
                    contentType = "image/gif";
                } else if (header.contains("webp")) {
                    contentType = "image/webp";
                }
                rawBase64 = base64Data.substring(base64Data.indexOf(",") + 1);
            }

            byte[] imageBytes = Base64.getDecoder().decode(rawBase64);
            String key = "events/" + UUID.randomUUID() + getExtension(contentType);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(imageBytes));

            String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
            logger.info("Image uploaded to S3: {}", url);
            return url;

        } catch (Exception e) {
            logger.error("Failed to upload image to S3", e);
            return null;
        }
    }

    public void deleteImage(String imageUrl) {
        if (!isEnabled() || imageUrl == null || !imageUrl.contains(bucketName)) {
            return;
        }

        try {
            String key = imageUrl.substring(imageUrl.indexOf(bucketName) + bucketName.length() + 1);
            if (key.startsWith("/")) {
                key = key.substring(1);
            }

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            logger.info("Image deleted from S3: {}", key);
        } catch (Exception e) {
            logger.error("Failed to delete image from S3", e);
        }
    }

    private String getExtension(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
