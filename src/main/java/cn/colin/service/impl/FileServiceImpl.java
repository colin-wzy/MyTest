package cn.colin.service.impl;

import cn.colin.common.entity.File;
import cn.colin.common.entity.User;
import cn.colin.common.office.OnlyOfficeConfig;
import cn.colin.common.request.*;
import cn.colin.common.response.FileListResponse;
import cn.colin.common.response.FilePreviewResponse;
import cn.colin.common.response.FileResponse;
import cn.colin.exceptions.FileServiceException;
import cn.colin.mapper.FileMapper;
import cn.colin.mapper.UserMapper;
import cn.colin.properties.OnlyOfficeProperties;
import cn.colin.service.FileService;
import cn.colin.utils.JsonUtil;
import cn.colin.utils.MinioUtil;
import cn.colin.utils.UserUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 文件服务实现类
 *
 * @author Administrator
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {
    @Resource
    private FileMapper fileMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private OnlyOfficeProperties onlyOfficeProperties;

    @Override
    public List<Long> uploadFiles(String bucketName, Long parentId, MultipartFile[] files) {
        List<Long> fileIds = new ArrayList<>();
        String timestamp = String.valueOf(System.currentTimeMillis());
        for (MultipartFile file : files) {
            try {
                String originalFilename = file.getOriginalFilename();
                if (StringUtils.isEmpty(originalFilename)) {
                    throw new FileServiceException("上传文件中的文件名不可为空");
                }
                if (checkFileNameExists(bucketName, parentId, originalFilename, false) > 0) {
                    throw new FileServiceException("同级目录下已存在同名文件: " + originalFilename);
                }
                String filePath = generateFilePathWithTimestamp(originalFilename, timestamp);
                MinioUtil.putObject(bucketName, filePath, file);
                File fileEntity = createFileEntity(bucketName, parentId, originalFilename, filePath, file.getSize(), file.getContentType());
                fileMapper.insert(fileEntity);
                fileIds.add(fileEntity.getId());
                log.info("批量上传文件成功: bucketName={}, fileName={}, fileId={}", bucketName, originalFilename, fileEntity.getId());
            } catch (FileServiceException e) {
                log.error("批量上传文件失败: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("批量上传文件失败: bucketName={}, fileName={}", bucketName, file.getOriginalFilename(), e);
            }
        }
        if (fileIds.isEmpty()) {
            throw new FileServiceException("所有文件上传失败");
        }
        return fileIds;
    }

    @Override
    public InputStream downloadFile(String bucketName, String fileName) {
        try {
            return MinioUtil.getObject(bucketName, fileName);
        } catch (Exception e) {
            log.error("下载文件失败: bucketName={}, fileName={}", bucketName, fileName, e);
            throw new FileServiceException("下载文件失败: " + e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String bucketName, String fileName) {
        try {
            return MinioUtil.getFileUrl(bucketName, fileName, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("获取文件URL失败: bucketName={}, fileName={}", bucketName, fileName, e);
            throw new FileServiceException("获取文件URL失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFolder(CreateFolderRequest request) {
        if (checkFileNameExists(request.getBucketName(), request.getParentId(), request.getFolderName(), true) > 0) {
            throw new FileServiceException("同级目录下已存在同名文件夹");
        }
        File folder = new File();
        folder.setFileName(request.getFolderName());
        folder.setBucketName(request.getBucketName());
        folder.setFilePath("folder_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 10000));
        folder.setParentId(request.getParentId());
        folder.setIsFolder(true);
        folder.setUserId(getCurrentUserId());
        fileMapper.insert(folder);
        log.info("创建文件夹成功: bucketName={}, folderName={}, folderId={}", request.getBucketName(), request.getFolderName(), folder.getId());
        return folder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFolder(DeleteFolderRequest request) {
        // 检查文件夹是否为空（没有子文件和子文件夹）
        Long count = fileMapper.selectCount(new LambdaQueryWrapper<File>()
                .eq(File::getBucketName, request.getBucketName())
                .eq(File::getParentId, request.getFolderId()));
        if (count > 0) {
            throw new FileServiceException("文件夹不为空，无法删除");
        }
        // 物理删除文件夹
        int deleted = fileMapper.deleteById(request.getFolderId());
        if (deleted > 0) {
            log.info("删除文件夹成功: bucketName={}, folderId={}", request.getBucketName(), request.getFolderId());
            return true;
        }
        return false;
    }

    @Override
    public List<FileResponse> getFolderList(String bucketName, Long parentId) {
        List<File> folders = fileMapper.selectList(new LambdaQueryWrapper<File>()
                .eq(File::getBucketName, bucketName)
                .eq(File::getParentId, parentId)
                .eq(File::getIsFolder, true)
                .orderByAsc(File::getFileName));
        return convertToFileResponseBatch(folders);
    }

    @Override
    public FileListResponse getFileList(GetFileListRequest request) {
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<File>()
                .eq(File::getBucketName, request.getBucketName())
                .eq(File::getParentId, request.getParentId());

        // 如果只查询文件夹
        if (Boolean.TRUE.equals(request.getFolderOnly())) {
            queryWrapper.eq(File::getIsFolder, true);
        }

        // 搜索关键字
        if (StringUtils.isNotEmpty(request.getSearchKeyword())) {
            queryWrapper.like(File::getFileName, request.getSearchKeyword());
        }

        // 分页
        int pageNum = request.getPageNum() != null ? request.getPageNum() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 20;
        Page<File> page = new Page<>(pageNum, pageSize);

        // 先统计总数
        Long total = fileMapper.selectCount(queryWrapper);

        // 查询列表，文件夹优先，然后按名称升序
        queryWrapper.orderByDesc(File::getIsFolder).orderByAsc(File::getFileName);
        Page<File> resultPage = fileMapper.selectPage(page, queryWrapper);

        List<FileResponse> files = convertToFileResponseBatch(resultPage.getRecords());

        return FileListResponse.builder()
                .files(files)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();
    }

    @Override
    public FilePreviewResponse getFilePreview(GetFilePreviewRequest request) {
        File file = fileMapper.selectById(request.getFileId());
        if (file == null) {
            throw new FileServiceException("文件不存在");
        }

        String contentType = file.getContentType();
        boolean isTextFile = contentType != null && contentType.startsWith("text/");
        boolean isImage = isImageContentType(contentType);
        boolean isOffice = isOfficeContentType(contentType);

        FilePreviewResponse.FilePreviewResponseBuilder builder = FilePreviewResponse.builder()
                .fileId(file.getId())
                .fileName(file.getFileName())
                .contentType(contentType);

        if (isTextFile) {
            return buildTextPreview(builder, request.getBucketName(), file.getFilePath());
        } else if (isImage) {
            return buildImagePreview(builder, request.getBucketName(), file.getFilePath());
        } else if (isOffice) {
            return buildOfficePreview(builder, file);
        } else {
            return builder.previewable(false).content("该文件类型不支持预览，请下载后查看").build();
        }
    }

    private FilePreviewResponse buildTextPreview(FilePreviewResponse.FilePreviewResponseBuilder builder,
                                                 String bucketName, String filePath) {
        try (InputStream in = MinioUtil.getObject(bucketName, filePath)) {
            if (in == null) {
                return builder.previewable(false).content("无法读取文件内容").build();
            }
            String content = IoUtil.read(in, StandardCharsets.UTF_8);
            return builder.previewable(true).previewType("text").content(content).build();
        } catch (Exception e) {
            log.error("文本预览失败: bucketName={}, filePath={}", bucketName, filePath, e);
            return builder.previewable(false).content("预览失败").build();
        }
    }

    private FilePreviewResponse buildImagePreview(FilePreviewResponse.FilePreviewResponseBuilder builder,
                                                  String bucketName, String filePath) {
        String imageUrl = MinioUtil.getFileUrl(bucketName, filePath, 30, TimeUnit.MINUTES);
        return builder.previewable(true).previewType("image").imageUrl(imageUrl).build();
    }

    private FilePreviewResponse buildOfficePreview(FilePreviewResponse.FilePreviewResponseBuilder builder, File file) {
        OnlyOfficeConfig config = buildOnlyOfficeConfig(file, "view", false);
        return builder.previewable(true).previewType("office").officeConfig(config).build();
    }

    @Override
    public OnlyOfficeConfig getOfficeEditConfig(GetFileEditRequest request) {
        File file = fileMapper.selectById(request.getFileId());
        if (file == null) {
            throw new FileServiceException("文件不存在");
        }
        if (!isEditableOfficeContentType(file.getContentType())) {
            throw new FileServiceException("该文件类型不支持在线编辑，仅支持 docx/xlsx/pptx 格式");
        }
        return buildOnlyOfficeConfig(file, "edit", true);
    }

    @Override
    public String handleOfficeCallback(String body) {
        log.info("收到OnlyOffice回调: body={}", body != null ? body.substring(0, Math.min(body.length(), 300)) : "null");
        try {
            JSONObject root = JsonUtil.parseObj(body);
            int status = root.getInt("status", 0);
            String key = root.getStr("key");

            // OnlyOffice 状态码: 1=编辑中, 2=已保存需下载, 3=保存出错, 4=关闭无变化, 6=强制保存
            log.info("OnlyOffice回调 status={}, key={}", status, key);

            if (status != 2 && status != 6) {
                return "{\"error\":0}";
            }

            String downloadUrl = root.getStr("url");
            if (downloadUrl == null || downloadUrl.isEmpty()) {
                log.error("OnlyOffice回调缺少下载URL");
                return "{\"error\":1,\"message\":\"缺少下载URL\"}";
            }

            // 从key中解析文件ID（key格式: fileId_updateTime）
            Long fileId = parseFileIdFromKey(key);
            if (fileId == null) {
                log.error("无法从key解析文件ID: {}", key);
                return "{\"error\":1,\"message\":\"无法解析文件ID\"}";
            }

            File file = fileMapper.selectById(fileId);
            if (file == null) {
                log.error("回调文件不存在: fileId={}", fileId);
                return "{\"error\":1,\"message\":\"文件不存在\"}";
            }

            byte[] editedBytes = HttpUtil.downloadBytes(downloadUrl);
            MinioUtil.putObject(file.getBucketName(), file.getFilePath(),
                    new ByteArrayInputStream(editedBytes), file.getContentType());

            // 更新数据库中的文件大小和更新时间
            fileMapper.update(null, new LambdaUpdateWrapper<File>()
                    .eq(File::getId, fileId)
                    .set(File::getFileSize, (long) editedBytes.length)
                    .set(File::getUpdateTime, new java.util.Date()));

            log.info("OnlyOffice回调保存成功: fileId={}, filePath={}, newSize={}",
                    fileId, file.getFilePath(), editedBytes.length);
            return "{\"error\":0}";

        } catch (Exception e) {
            log.error("处理OnlyOffice回调失败", e);
            return "{\"error\":1,\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    private OnlyOfficeConfig buildOnlyOfficeConfig(File file, String mode, boolean editable) {
        String fileUrl = MinioUtil.getFileUrl(file.getBucketName(), file.getFilePath(), 1, TimeUnit.HOURS);
        long updateTimestamp = file.getUpdateTime() != null
                ? file.getUpdateTime().getTime() : System.currentTimeMillis();
        String fileKey = file.getId() + "_" + updateTimestamp;
        cn.colin.common.entity.User user = UserUtil.getUser();
        String userId = user != null ? user.getId().toString() : "anonymous";
        String userName = user != null ? user.getUserName() : "anonymous";

        OnlyOfficeConfig.Document document = OnlyOfficeConfig.Document.builder()
                .fileType(getFileExtension(file.getFileName()))
                .key(fileKey)
                .title(file.getFileName())
                .url(fileUrl)
                .permissions(OnlyOfficeConfig.Permissions.builder()
                        .edit(editable)
                        .download(true)
                        .print(true)
                        .review(editable)
                        .comment(editable)
                        .fillForms(editable)
                        .modifyFilter(editable)
                        .build())
                .build();

        OnlyOfficeConfig.Customization customization = OnlyOfficeConfig.Customization.builder()
                .autosave(true)
                .compactHeader(false)
                .compactToolbar(false)
                .build();

        OnlyOfficeConfig.EditorConfig editorConfig = OnlyOfficeConfig.EditorConfig.builder()
                .callbackUrl(onlyOfficeProperties.getCallbackUrl() + "/file/office/callback")
                .mode(mode)
                .lang("zh-CN")
                .user(OnlyOfficeConfig.User.builder()
                        .id(userId)
                        .name(userName)
                        .build())
                .customization(customization)
                .build();

        return OnlyOfficeConfig.builder()
                .document(document)
                .editorConfig(editorConfig)
                .height("100%")
                .width("100%")
                .type("desktop")
                .documentType(mapToDocumentCategory(file.getContentType()))
                .docServerUrl(onlyOfficeProperties.getDocServerUrl())
                .build();
    }

    private static final Set<String> OFFICE_TYPES = Set.of(
            "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/pdf");

    private static final Set<String> EDITABLE_OFFICE_TYPES = Set.of(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/pdf");

    private boolean isImageContentType(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }

    private boolean isOfficeContentType(String contentType) {
        return contentType != null && OFFICE_TYPES.contains(contentType);
    }

    private boolean isEditableOfficeContentType(String contentType) {
        return contentType != null && EDITABLE_OFFICE_TYPES.contains(contentType);
    }

    private String getFileExtension(String fileName) {
        return fileName != null && fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase() : "docx";
    }

    private String mapToDocumentCategory(String contentType) {
        if (contentType == null) return "word";
        if (contentType.contains("word")) return "word";
        if (contentType.contains("spreadsheet") || contentType.contains("excel")) return "cell";
        if (contentType.contains("presentation") || contentType.contains("powerpoint")) return "slide";
        if (contentType.contains("pdf")) return "pdf";
        return "word";
    }

    private Long parseFileIdFromKey(String key) {
        if (key == null || !key.contains("_")) return null;
        try {
            return Long.parseLong(key.substring(0, key.indexOf('_')));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean moveFile(MoveFileRequest request) {
        File file = getFileByIdOrThrow(request.getFileId());
        validateTargetFolder(request.getTargetParentId());
        if (checkFileNameExists(request.getBucketName(), request.getTargetParentId(), file.getFileName(), file.getIsFolder()) > 0
                && !file.getId().equals(request.getFileId())) {
            throw new FileServiceException("目标目录下已存在同名文件");
        }
        fileMapper.update(null, new LambdaUpdateWrapper<File>()
                .eq(File::getId, request.getFileId())
                .set(File::getParentId, request.getTargetParentId()));
        log.info("移动文件成功: fileId={}, targetParentId={}", request.getFileId(), request.getTargetParentId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyFile(CopyFileRequest request) {
        File file = getFileByIdOrThrow(request.getFileId());
        if (Boolean.TRUE.equals(file.getIsFolder())) {
            throw new FileServiceException("不支持复制文件夹");
        }
        validateTargetFolder(request.getTargetParentId());
        if (checkFileNameExists(request.getBucketName(), request.getTargetParentId(), file.getFileName(), false) > 0) {
            throw new FileServiceException("目标目录下已存在同名文件: " + file.getFileName());
        }
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String newFilePath = generateFilePathWithTimestamp(file.getFileName(), timestamp);
            File newFile = createFileEntity(request.getBucketName(), request.getTargetParentId(),
                    file.getFileName(), newFilePath, file.getFileSize(), file.getContentType());
            fileMapper.insert(newFile);
            MinioUtil.copyObject(request.getBucketName(), file.getFilePath(), newFilePath);
            log.info("复制文件成功: fileId={}, newFileId={}, targetParentId={}, newFilePath={}", request.getFileId(), newFile.getId(), request.getTargetParentId(), newFilePath);
            return newFile.getId();
        } catch (FileServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("复制文件失败: fileId={}", request.getFileId(), e);
            throw new FileServiceException("复制文件失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean renameFile(RenameFileRequest request) {
        File file = getFileByIdOrThrow(request.getFileId());
        if (checkFileNameExists(request.getBucketName(), file.getParentId(), request.getNewName(), file.getIsFolder()) > 0) {
            throw new FileServiceException("同级目录下已存在同名文件");
        }
        fileMapper.update(null, new LambdaUpdateWrapper<File>()
                .eq(File::getId, request.getFileId())
                .set(File::getFileName, request.getNewName()));
        log.info("重命名成功: fileId={}, oldName={}, newName={}, filePath={}", request.getFileId(), file.getFileName(), request.getNewName(), file.getFilePath());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(BatchDeleteRequest request) {
        int deleteCount = 0;
        for (Long fileId : request.getFileIds()) {
            File file = fileMapper.selectById(fileId);
            if (file != null) {
                if (Boolean.TRUE.equals(file.getIsFolder())) {
                    // 是文件夹，先检查是否为空
                    Long childCount = fileMapper.selectCount(new LambdaQueryWrapper<File>()
                            .eq(File::getBucketName, request.getBucketName())
                            .eq(File::getParentId, fileId));
                    if (childCount > 0) {
                        log.warn("文件夹不为空，无法删除: folderId={}, folderName={}", fileId, file.getFileName());
                        throw new FileServiceException("文件夹 " + file.getFileName() + " 不为空，无法删除");
                    }
                } else {
                    // 是文件，从MinIO删除
                    try {
                        MinioUtil.removeObject(request.getBucketName(), file.getFilePath());
                    } catch (Exception e) {
                        log.error("从MinIO删除文件失败: filePath={}", file.getFilePath(), e);
                        // 继续处理，记录错误但不中断流程
                    }
                }
                // 物理删除
                fileMapper.deleteById(fileId);
                deleteCount++;
                log.info("批量删除文件成功: fileId={}, fileName={}", fileId, file.getFileName());
            }
        }
        return deleteCount;
    }

    @Override
    public FileResponse getFileById(Long fileId) {
        File file = fileMapper.selectById(fileId);
        if (file == null) {
            return null;
        }
        return convertToFileResponseBatch(Collections.singletonList(file)).getFirst();
    }

    /**
     * 检查目标目录下是否存在同名文件或文件夹
     */
    private Long checkFileNameExists(String bucketName, Long parentId, String fileName, Boolean isFolder) {
        return fileMapper.selectCount(new LambdaQueryWrapper<File>()
                .eq(File::getBucketName, bucketName)
                .eq(File::getParentId, parentId)
                .eq(File::getFileName, fileName)
                .eq(File::getIsFolder, isFolder));
    }

    /**
     * 验证目标文件夹存在且为文件夹类型
     */
    private void validateTargetFolder(Long targetParentId) {
        if (targetParentId != 0) {
            File targetFolder = fileMapper.selectById(targetParentId);
            if (targetFolder == null || !Boolean.TRUE.equals(targetFolder.getIsFolder())) {
                throw new FileServiceException("目标文件夹不存在");
            }
        }
    }

    /**
     * 根据ID获取文件，不存在则抛异常
     */
    private File getFileByIdOrThrow(Long fileId) {
        File file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new FileServiceException("文件不存在");
        }
        return file;
    }

    /**
     * 创建文件实体
     */
    private File createFileEntity(String bucketName, Long parentId, String fileName, String filePath,
                                  Long fileSize, String contentType) {
        File fileEntity = new File();
        fileEntity.setBucketName(bucketName);
        fileEntity.setParentId(parentId);
        fileEntity.setFileName(fileName);
        fileEntity.setFilePath(filePath);
        fileEntity.setFileSize(fileSize);
        fileEntity.setContentType(contentType);
        fileEntity.setIsFolder(false);
        fileEntity.setUserId(getCurrentUserId());
        return fileEntity;
    }

    /**
     * 批量转换File实体为FileResponse（解决N+1查询问题）
     */
    private List<FileResponse> convertToFileResponseBatch(List<File> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }
        // 收集所有非空的userId
        List<Long> userIds = files.stream()
                .map(File::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 批量查询用户信息
        Map<Long, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            users.forEach(user -> userNameMap.put(user.getId(), user.getUserName()));
        }

        // 转换文件列表
        return files.stream()
                .map(file -> convertToFileResponse(file, userNameMap))
                .collect(Collectors.toList());
    }

    /**
     * 转换File实体为FileResponse（使用预查询的用户信息）
     */
    private FileResponse convertToFileResponse(File file, Map<Long, String> userNameMap) {
        String username = file.getUserId() != null ? userNameMap.get(file.getUserId()) : null;
        return FileResponse.builder()
                .id(file.getId())
                .fileName(file.getFileName())
                .filePath(file.getFilePath())
                .fileSize(file.getFileSize())
                .contentType(file.getContentType())
                .bucketName(file.getBucketName())
                .parentId(file.getParentId())
                .isFolder(file.getIsFolder())
                .createTime(file.getCreateTime())
                .updateTime(file.getUpdateTime())
                .username(username)
                .build();
    }

    /**
     * 生成带时间戳的文件路径：原始文件名_时间戳.扩展名
     */
    private String generateFilePathWithTimestamp(String originalFilename, String timestamp) {
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            String name = originalFilename.substring(0, lastDotIndex);
            String extension = originalFilename.substring(lastDotIndex);
            return name + "_" + timestamp + extension;
        } else {
            return originalFilename + "_" + timestamp;
        }
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        User user = UserUtil.getUser();
        if (user == null) {
            throw new FileServiceException("用户未登录");
        }
        return user.getId();
    }
}