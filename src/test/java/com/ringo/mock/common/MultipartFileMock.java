package com.ringo.mock.common;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class MultipartFileMock {
    public static MultipartFile getMockMultipartFile() {
        return new MockMultipartFile(
                "file",
                "hello.txt",
                "image/png",
                "Test".getBytes()
        );
    }
}
