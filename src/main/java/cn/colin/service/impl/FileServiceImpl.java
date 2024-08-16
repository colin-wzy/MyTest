package cn.colin.service.impl;

import cn.colin.service.FileService;
import cn.colin.utils.MinioUtil;
import io.minio.ObjectWriteResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author Administrator
 */
@Service
public class FileServiceImpl implements FileService {

    @Override
    public ObjectWriteResponse uploadFile(String bucketName, MultipartFile file) {
        return MinioUtil.uploadFile(bucketName, file);
    }

    @Override
    public ObjectWriteResponse uploadBigFile(String bucketName, MultipartFile file) {
        return MinioUtil.uploadFile(bucketName, file);
    }
}
