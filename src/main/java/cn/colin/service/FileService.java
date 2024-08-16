package cn.colin.service;

import io.minio.ObjectWriteResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    ObjectWriteResponse uploadFile(String bucketName, MultipartFile file);

    ObjectWriteResponse uploadBigFile(String bucketName, MultipartFile file);
}
