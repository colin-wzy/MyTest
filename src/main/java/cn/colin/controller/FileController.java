package cn.colin.controller;

import cn.colin.common.Response;
import cn.colin.entity.MinioFile;
import cn.colin.service.FileService;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


/**
 * @author Administrator
 */
@RestController
@RequestMapping(value = "/file")
public class FileController {
    @Resource
    private FileService fileService;

    @PostMapping("/uploadFile")
    public Response<Boolean> uploadFile(@RequestParam("bucketName") String bucketName,
                                        @RequestParam("file") MultipartFile file) {
        return Response.success(fileService.uploadFile(bucketName, file));
    }

    @PostMapping("/uploadBigFile")
    public Response<Boolean> uploadBigFile(@RequestParam("bucketName") String bucketName,
                                           @RequestParam("file") MultipartFile file) {
        return Response.success(fileService.uploadBigFile(bucketName, file));
    }

    @SneakyThrows
    @PostMapping("/downloadFile")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("bucketName") String bucketName,
                                               @RequestParam("fileName") String fileName) {
        InputStream inputStream = fileService.downloadFile(bucketName, fileName);
        byte[] result = inputStream.readAllBytes();
        inputStream.close();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        return ResponseEntity.ok().headers(headers).body(result);
    }

    @PostMapping("/downloadFileAs")
    public Response<Boolean> downloadFileAs(@RequestParam("bucketName") String bucketName,
                                            @RequestParam("fileName") String fileName,
                                            @RequestParam("filePath") String filePath) {
        return Response.success(fileService.downloadFileAs(bucketName, fileName, filePath));
    }

    @PostMapping("/getFileUrl")
    public Response<String> getFileUrl(@RequestParam("bucketName") String bucketName,
                                       @RequestParam("fileName") String fileName) {
        return Response.success(fileService.getFileUrl(bucketName, fileName));
    }

    @PostMapping("/statFile")
    public Response<MinioFile> statFile(@RequestParam("bucketName") String bucketName,
                                        @RequestParam("fileName") String fileName) {
        return Response.success(fileService.statFile(bucketName, fileName));
    }

    @PostMapping("/deleteFile")
    public Response<Boolean> deleteFile(@RequestParam("bucketName") String bucketName,
                                        @RequestParam("fileName") String fileName) {
        return Response.success(fileService.deleteFile(bucketName, fileName));
    }
}
