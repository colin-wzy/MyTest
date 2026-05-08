package cn.colin.controller;

import cn.colin.common.office.OnlyOfficeConfig;
import cn.colin.common.request.*;
import cn.colin.common.response.FileListResponse;
import cn.colin.common.response.FilePreviewResponse;
import cn.colin.common.response.FileResponse;
import cn.colin.common.response.Response;
import cn.colin.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 文件控制器
 *
 * @author Administrator
 */
@Slf4j
@RestController
@RequestMapping(value = "/file")
@Tag(name = "文件管理", description = "文件管理相关接口")
public class FileController {
    @Resource
    private FileService fileService;

    // ==================== 文件列表接口 ====================

    /**
     * 文件列表查询
     */
    @PostMapping("/list")
    @Operation(summary = "文件列表查询", description = "分页查询文件列表，支持搜索和文件夹过滤")
    public Response<FileListResponse> list(@RequestBody @Valid GetFileListRequest request) {
        return Response.success(fileService.getFileList(request));
    }

    /**
     * 文件夹列表查询
     */
    @PostMapping("/folderList")
    @Operation(summary = "文件夹列表查询", description = "查询指定目录下的文件夹列表")
    public Response<List<FileResponse>> folderList(@RequestBody @Valid GetFolderListRequest request) {
        return Response.success(fileService.getFolderList(request.getBucketName(), request.getParentId()));
    }

    // ==================== 文件夹管理接口 ====================

    /**
     * 创建文件夹
     */
    @PostMapping("/createFolder")
    @Operation(summary = "创建文件夹", description = "在指定目录下创建新文件夹")
    public Response<Long> createFolder(@RequestBody @Valid CreateFolderRequest request) {
        Long folderId = fileService.createFolder(request);
        if (folderId != null) {
            return Response.success(folderId);
        }
        return Response.failed("创建文件夹失败");
    }

    /**
     * 删除文件夹
     */
    @PostMapping("/deleteFolder")
    @Operation(summary = "删除文件夹", description = "删除空文件夹")
    public Response<Boolean> deleteFolder(@RequestBody @Valid DeleteFolderRequest request) {
        if (fileService.deleteFolder(request)) {
            return Response.success();
        }
        return Response.failed("删除文件夹失败");
    }

    // ==================== 文件操作接口 ====================

    /**
     * 重命名文件/文件夹
     */
    @PostMapping("/rename")
    @Operation(summary = "重命名文件/文件夹", description = "修改文件或文件夹的名称")
    public Response<Boolean> rename(@RequestBody @Valid RenameFileRequest request) {
        if (fileService.renameFile(request)) {
            return Response.success();
        }
        return Response.failed("重命名失败");
    }

    /**
     * 删除文件/文件夹
     */
    @PostMapping("/delete")
    @Operation(summary = "删除文件/文件夹", description = "删除文件或空文件夹")
    public Response<Void> delete(@RequestBody @Valid DeleteFileRequest request) {
        Long actualFileId = Long.parseLong(request.getFileId());
        BatchDeleteRequest batchRequest = new BatchDeleteRequest();
        batchRequest.setBucketName(request.getBucketName());
        batchRequest.setFileIds(List.of(actualFileId));
        int count = fileService.batchDelete(batchRequest);
        if (count > 0) {
            return Response.success();
        }
        return Response.failed("删除失败");
    }

    /**
     * 批量删除文件/文件夹
     */
    @PostMapping("/batchDelete")
    @Operation(summary = "批量删除文件/文件夹", description = "批量删除文件或空文件夹")
    public Response<Integer> batchDelete(@RequestBody @Valid BatchDeleteRequest request) {
        return Response.success(fileService.batchDelete(request));
    }

    /**
     * 移动文件/文件夹
     */
    @PostMapping("/move")
    @Operation(summary = "移动文件/文件夹", description = "将文件或文件夹移动到目标目录")
    public Response<Boolean> move(@RequestBody @Valid MoveFileRequest request) {
        if (fileService.moveFile(request)) {
            return Response.success();
        }
        return Response.failed("移动文件失败");
    }

    /**
     * 复制文件
     */
    @PostMapping("/copy")
    @Operation(summary = "复制文件", description = "复制文件到目标目录，文件夹不支持复制")
    public Response<Long> copy(@RequestBody @Valid CopyFileRequest request) {
        Long newFileId = fileService.copyFile(request);
        if (newFileId != null) {
            return Response.success(newFileId);
        }
        return Response.failed("复制文件失败");
    }

    // ==================== 文件预览接口 ====================

    /**
     * 文件预览
     */
    @PostMapping("/preview")
    @Operation(summary = "文件预览", description = "获取文件预览内容，支持文本和图片类型")
    public Response<FilePreviewResponse> preview(@RequestBody @Valid GetFilePreviewRequestVO request) {
        Long actualFileId = Long.parseLong(request.getFileId());
        return Response.success(fileService.getFilePreview(new GetFilePreviewRequest(request.getBucketName(), actualFileId)));
    }

    // ==================== OnlyOffice 接口 ====================

    /**
     * 获取文档编辑配置
     */
    @PostMapping("/office/edit")
    @Operation(summary = "获取文档编辑配置", description = "获取OnlyOffice在线编辑配置，仅支持docx/xlsx/pptx格式")
    public Response<OnlyOfficeConfig> officeEdit(@RequestBody @Valid GetFileEditRequest request) {
        return Response.success(fileService.getOfficeEditConfig(request));
    }

    /**
     * OnlyOffice保存回调（返回 application/json，OnlyOffice 严格要求此 Content-Type）
     */
    @PostMapping("/office/callback")
    @Operation(summary = "OnlyOffice保存回调", description = "OnlyOffice编辑保存后的回调接口")
    public ResponseEntity<String> officeCallback(@RequestBody String body) {
        String result = fileService.handleOfficeCallback(body);
        log.info("OnlyOffice回调处理结果: {}", result);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
    }

    // ==================== 文件上传下载接口 ====================

    /**
     * 批量文件上传
     */
    @PostMapping("/uploadMultiple")
    @Operation(summary = "批量文件上传", description = "批量上传多个文件到指定目录")
    public Response<List<Long>> uploadMultiple(
            @Parameter(description = "存储桶名称", required = true) @RequestParam @NotBlank(message = "存储桶名称不能为空") String bucketName,
            @Parameter(description = "父文件夹ID") @RequestParam(defaultValue = "0") Long parentId,
            @Parameter(description = "文件列表", required = true) @RequestParam("files") @NotNull(message = "文件不能为空") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Response.failed("没有上传文件");
        }
        List<Long> fileIds = fileService.uploadFiles(bucketName, parentId, files);
        return Response.success(fileIds);
    }

    /**
     * 文件下载
     */
    @PostMapping("/download")
    @Operation(summary = "文件下载", description = "下载指定文件")
    public ResponseEntity<byte[]> download(@RequestBody @Valid DownloadFileRequest request) {
        Long actualFileId = Long.parseLong(request.getFileId());
        FileResponse fileResponse = fileService.getFileById(actualFileId);
        if (fileResponse == null) {
            return ResponseEntity.notFound().build();
        }

        InputStream inputStream = fileService.downloadFile(request.getBucketName(), fileResponse.getFilePath());
        if (inputStream == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] result;
        try (inputStream) {
            result = inputStream.readAllBytes();
        } catch (Exception e) {
            log.error("读取文件失败", e);
            return ResponseEntity.internalServerError().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", URLEncoder.encode(fileResponse.getFileName(), StandardCharsets.UTF_8));
        return ResponseEntity.ok().headers(headers).body(result);
    }

    /**
     * 获取文件临时访问URL
     */
    @PostMapping("/getFileUrl")
    @Operation(summary = "获取文件访问URL", description = "获取文件的临时访问链接")
    public Response<String> getFileUrl(@RequestBody @Valid GetFileUrlRequest request) {
        Long actualFileId = Long.parseLong(request.getFileId());
        FileResponse fileResponse = fileService.getFileById(actualFileId);
        if (fileResponse == null) {
            return Response.failed("文件不存在");
        }
        return Response.success(fileService.getFileUrl(request.getBucketName(), fileResponse.getFilePath()));
    }

    /**
     * 搜索文件
     */
    @PostMapping("/search")
    @Operation(summary = "搜索文件", description = "根据关键字搜索文件")
    public Response<FileListResponse> search(@RequestBody @Valid SearchFileRequest request) {
        GetFileListRequest listRequest = new GetFileListRequest();
        listRequest.setBucketName(request.getBucketName());
        listRequest.setParentId(0L);
        listRequest.setSearchKeyword(request.getKeyword());
        listRequest.setPageNum(request.getPageNum());
        listRequest.setPageSize(request.getPageSize());
        return Response.success(fileService.getFileList(listRequest));
    }

}
