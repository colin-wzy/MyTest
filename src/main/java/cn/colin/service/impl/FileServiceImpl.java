package cn.colin.service.impl;

import cn.colin.entity.MinioFile;
import cn.colin.service.FileService;
import cn.colin.utils.MinioUtil;
import io.minio.StatObjectResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * @author Administrator
 */
@Service
public class FileServiceImpl implements FileService {

    @Override
    public boolean uploadFile(String bucketName, MultipartFile file) {
        try {
            MinioUtil.putObject(bucketName, file);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean uploadBigFile(String bucketName, MultipartFile file) {
        try {
            MinioUtil.uploadObject(bucketName, file);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public InputStream downloadFile(String bucketName, String fileName) {
        return MinioUtil.getObject(bucketName, fileName);
    }

    @Override
    public boolean downloadFileAs(String bucketName, String fileName, String filePath) {
        try {
            InputStream stream = MinioUtil.getObject(bucketName, fileName);
            if (stream == null) {
                return false;
            }
            Files.copy(stream, Paths.get(filePath));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getFileUrl(String bucketName, String fileName) {
        return MinioUtil.getFileUrl(bucketName, fileName, 5, TimeUnit.MINUTES);
    }

    @Override
    public MinioFile statFile(String bucketName, String fileName) {
        StatObjectResponse statObjectResponse = MinioUtil.statObject(bucketName, fileName);
        if (statObjectResponse != null) {
            ZonedDateTime zonedDateTime = statObjectResponse.lastModified();
            Date date = Date.from(zonedDateTime.toInstant());
            return MinioFile.builder().bucketName(statObjectResponse.bucket())
                    .fileName(statObjectResponse.object())
                    .size(statObjectResponse.size()).lastModifiedTime(date).build();
        }
        return null;
    }

    @Override
    public boolean deleteFile(String bucketName, String fileName) {
        try {
            MinioUtil.removeObject(bucketName, fileName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
