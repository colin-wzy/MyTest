package cn.colin.service;

import cn.colin.entity.MinioFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileService {

    boolean uploadFile(String bucketName, MultipartFile file);

    boolean uploadBigFile(String bucketName, MultipartFile file);

    InputStream downloadFile(String bucketName, String fileName);

    boolean downloadFileAs(String bucketName, String fileName, String filePath);

    String getFileUrl(String bucketName, String fileName);

    MinioFile statFile(String bucketName, String fileName);

    boolean deleteFile(String bucketName, String fileName);
}
