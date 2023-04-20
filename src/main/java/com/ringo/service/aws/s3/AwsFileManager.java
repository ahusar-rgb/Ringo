package com.ringo.service.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AwsFileManager {

    private static final String BUCKET_NAME = "ringo-photos";

    private final AmazonS3 s3;

    public void uploadFile(String fileName, byte[] bytes) {

        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, fileName, new ByteArrayInputStream(bytes), null);

        s3.putObject(putObjectRequest);
    }

    public void deleteFile(String fileName) {
        s3.deleteObject(BUCKET_NAME, fileName);
    }

    public byte[] getFile(String fileName) throws IOException {
        return s3.getObject(BUCKET_NAME, fileName).getObjectContent().readAllBytes();
    }
}
