package cn.colin.service.impl;

import cn.colin.service.FileService;
import cn.colin.utils.MinioUtil;
import io.minio.ObjectWriteResponse;
import io.minio.StatObjectResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;


/**
 * @author Administrator
 */
@Service
public class FileServiceImpl implements FileService {

    @Override
    public ObjectWriteResponse uploadFile(String bucketName, MultipartFile file) {
        return MinioUtil.putObject(bucketName, file);
    }

    @Override
    public ObjectWriteResponse uploadBigFile(String bucketName, MultipartFile file) {
        return MinioUtil.uploadFile(bucketName, file);
    }

    @Override
    public String getFileUrl(String bucketName, String fileName) {
        return MinioUtil.getFileUrl(bucketName, fileName, 5, TimeUnit.MINUTES);
    }

    @Override
    public StatObjectResponse statObject(String bucketName, String fileName) {
        return MinioUtil.statObject(bucketName, fileName);
    }
}
