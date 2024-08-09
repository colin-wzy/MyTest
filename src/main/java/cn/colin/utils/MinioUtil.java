package cn.colin.utils;

import com.google.common.collect.Lists;
import io.minio.*;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class MinioUtil {
    private static MinioClient minioClient;

    @Resource
    public void setMinioClient(MinioClient minioClient) {
        MinioUtil.minioClient = minioClient;
    }

    @SneakyThrows
    public static boolean bucketExists(String bucketName) {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    @SneakyThrows
    public static void makeBucket(String bucketName) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            return;
        }
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    }

    @SneakyThrows
    public static List<Bucket> listBuckets() {
        return minioClient.listBuckets();
    }

    @SneakyThrows
    public static List<String> listBucketNames() {
        List<Bucket> list = listBuckets();
        return list.stream().filter(Objects::nonNull).map(Bucket::name).collect(Collectors.toList());
    }

    @SneakyThrows
    public static Iterable<Result<Item>> listObjects(String bucketName) {
        return minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
    }

    @SneakyThrows
    public static List<String> listObjectNames(String bucketName) {
        List<String> result = Lists.newArrayList();
        Iterable<Result<Item>> myObjects = listObjects(bucketName);
        for (Result<Item> itemResult : myObjects) {
            Item item = itemResult.get();
            result.add(item.objectName());
        }
        return result;
    }

    @SneakyThrows
    public static boolean removeBucket(String bucketName) {
        Iterable<Result<Item>> myObjects = listObjects(bucketName);
        for (Result<Item> result : myObjects) {
            Item item = result.get();
            // 有对象文件，则删除失败
            if (item.size() > 0) {
                return false;
            }
        }
        // 删除存储桶，注意，只有存储桶为空时才能删除成功。
        minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
        return !bucketExists(bucketName);
    }

    @SneakyThrows
    public static ObjectWriteResponse putObject(String bucketName, MultipartFile multipartFile) {
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(multipartFile.getName())
                .contentType(multipartFile.getContentType())
                .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1)
                .build();
        return minioClient.putObject(args);
    }

    @SneakyThrows
    public static ObjectWriteResponse putObject(String bucketName, String objectName, InputStream in, String contentType) {
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .contentType(contentType)
                .stream(in, in.available(), -1)
                .build();
        return minioClient.putObject(args);
    }

    @SneakyThrows
    public static InputStream getObject(String bucketName, String objectName) {
        boolean flag = bucketExists(bucketName);
        if (!flag) {
            return null;
        }

        StatObjectResponse resp = statObject(bucketName, objectName);
        return resp == null ? null : minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName).object(objectName).build());
    }

    @SneakyThrows
    public static void getObject(String bucketName, String objectName, String filePath) {
        InputStream stream = getObject(bucketName, objectName);
        if (stream == null) {
            return;
        }
        Files.copy(stream, Paths.get(filePath));
    }

    @SneakyThrows
    public static StatObjectResponse statObject(String bucketName, String objectName) {
        return !bucketExists(bucketName) ? null : minioClient.statObject(StatObjectArgs.builder()
                .bucket(bucketName).object(objectName).build());
    }

    @SneakyThrows
    public static void removeMinio(String bucketName, String fileName) {
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
    }
}
