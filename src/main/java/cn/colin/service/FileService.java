package cn.colin.service;

import io.minio.ObjectWriteResponse;
import io.minio.StatObjectResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    ObjectWriteResponse uploadFile(String bucketName, MultipartFile file);

    ObjectWriteResponse uploadBigFile(String bucketName, MultipartFile file);

    String getFileUrl(String bucketName, String fileName);

    StatObjectResponse statObject(String bucketName, String fileName);
}
