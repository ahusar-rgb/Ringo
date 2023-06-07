package com.ringo.service.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ringo.exception.InternalException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

@Component
@RequiredArgsConstructor
public class AwsFileManager {

    private static final String BUCKET_NAME = "ringo-photos";

    @Value("${AWS_WORKING_DIR}")
    private String WORKING_DIRECTORY;

    private final AmazonS3 s3;

    public void uploadFile(String fileName, byte[] bytes) {

        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, WORKING_DIRECTORY + fileName, new ByteArrayInputStream(bytes), null);

        s3.putObject(putObjectRequest);
    }

    public void deleteFile(String fileName) {
        s3.deleteObject(BUCKET_NAME, WORKING_DIRECTORY + fileName);
    }

    public byte[] getFile(String fileName) {
        try {
            return s3.getObject(BUCKET_NAME, WORKING_DIRECTORY + fileName).getObjectContent().readAllBytes();
        }
        catch (Exception e) {
            throw new InternalException("Failed to read file");
        }
    }
}
