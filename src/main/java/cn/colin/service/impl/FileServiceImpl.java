package cn.colin.service.impl;

import cn.colin.common.entity.File;
import cn.colin.common.entity.User;
import cn.colin.common.request.*;
import cn.colin.common.response.FileListResponse;
import cn.colin.common.response.FilePreviewResponse;
import cn.colin.common.response.FileResponse;
import cn.colin.exceptions.BusinessException;
import cn.colin.mapper.FileMapper;
import cn.colin.mapper.UserMapper;
import cn.colin.service.FileService;
import cn.colin.utils.MinioUtil;
import cn.colin.utils.UserUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

    // ==================== 文件上传下载 ====================

    @Override
    public List<Long> uploadFiles(String bucketName, Long parentId, MultipartFile[] files) {
        List<Long> fileIds = new ArrayList<>();
        String timestamp = String.valueOf(System.currentTimeMillis());
        for (MultipartFile file : files) {
            try {
                String originalFilename = file.getOriginalFilename();
                if (checkFileNameExists(bucketName, parentId, originalFilename, false) > 0) {
                    throw new BusinessException("同级目录下已存在同名文件: " + originalFilename);
                }
                String filePath = generateFilePathWithTimestamp(originalFilename, timestamp);
                MinioUtil.putObject(bucketName, filePath, file);
                File fileEntity = createFileEntity(bucketName, parentId, originalFilename, filePath, file.getSize(), file.getContentType(), false);
                fileMapper.insert(fileEntity);
                fileIds.add(fileEntity.getId());
                log.info("批量上传文件成功: bucketName={}, fileName={}, fileId={}", bucketName, originalFilename, fileEntity.getId());
            } catch (BusinessException e) {
                log.error("批量上传文件失败: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("批量上传文件失败: bucketName={}, fileName={}", bucketName, file.getOriginalFilename(), e);
            }
        }
        if (fileIds.isEmpty()) {
            throw new BusinessException("所有文件上传失败");
        }
        return fileIds;
    }

    @Override
    public InputStream downloadFile(String bucketName, String fileName) {
        try {
            return MinioUtil.getObject(bucketName, fileName);
        } catch (Exception e) {
            log.error("下载文件失败: bucketName={}, fileName={}", bucketName, fileName, e);
            throw new BusinessException("下载文件失败: " + e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String bucketName, String fileName) {
        try {
            return MinioUtil.getFileUrl(bucketName, fileName, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("获取文件URL失败: bucketName={}, fileName={}", bucketName, fileName, e);
            throw new BusinessException("获取文件URL失败: " + e.getMessage());
        }
    }

    // ==================== 文件夹管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFolder(CreateFolderRequest request) {
        if (checkFileNameExists(request.getBucketName(), request.getParentId(), request.getFolderName(), true) > 0) {
            throw new BusinessException("同级目录下已存在同名文件夹");
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
            throw new BusinessException("文件夹不为空，无法删除");
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
        return folders.stream().map(this::convertToFileResponse).collect(Collectors.toList());
    }

    // ==================== 文件列表 ====================

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
        if (StringUtils.hasText(request.getSearchKeyword())) {
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

        List<FileResponse> files = resultPage.getRecords().stream()
                .map(this::convertToFileResponse)
                .collect(Collectors.toList());

        return FileListResponse.builder()
                .files(files)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();
    }

    // ==================== 文件预览 ====================

    @Override
    public FilePreviewResponse getFilePreview(GetFilePreviewRequest request) {
        File file = fileMapper.selectById(request.getFileId());
        if (file == null) {
            throw new BusinessException("文件不存在");
        }

        String contentType = file.getContentType();
        boolean isTextFile = contentType != null && contentType.startsWith("text/");
        boolean isWord = isWordContentType(contentType);
        boolean isExcel = isExcelContentType(contentType);
        boolean previewable = isTextFile || isWord || isExcel;

        FilePreviewResponse.FilePreviewResponseBuilder builder = FilePreviewResponse.builder()
                .fileId(file.getId())
                .fileName(file.getFileName())
                .contentType(contentType)
                .previewable(previewable);

        if (!previewable) {
            builder.content("该文件类型不支持预览，请下载后查看");
            return builder.build();
        }

        try (InputStream inputStream = MinioUtil.getObject(request.getBucketName(), file.getFilePath())) {
            if (inputStream == null) {
                builder.content("无法读取文件内容").previewable(false);
            } else if (isTextFile) {
                builder.content(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
            } else if (isWord) {
                builder.content(extractWordText(inputStream));
            } else if (isExcel) {
                builder.content(extractExcelText(inputStream));
            }
        } catch (Exception e) {
            log.error("预览文件失败: fileId={}", request.getFileId(), e);
            builder.content("预览失败").previewable(false);
        }

        return builder.build();
    }

    /**
     * 判断是否为Word文档类型
     */
    private boolean isWordContentType(String contentType) {
        if (contentType == null) return false;
        return contentType.equals("application/msword") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    /**
     * 判断是否为Excel文档类型
     */
    private boolean isExcelContentType(String contentType) {
        if (contentType == null) return false;
        return contentType.equals("application/vnd.ms-excel") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    /**
     * 从Word文档提取文本内容
     */
    private String extractWordText(InputStream inputStream) throws Exception {
        org.apache.poi.xwpf.usermodel.XWPFDocument document = new org.apache.poi.xwpf.usermodel.XWPFDocument(inputStream);
        StringBuilder sb = new StringBuilder();
        for (org.apache.poi.xwpf.usermodel.XWPFParagraph paragraph : document.getParagraphs()) {
            if (!sb.isEmpty()) {
                sb.append("\n");
            }
            sb.append(paragraph.getText());
        }
        document.close();
        return sb.toString();
    }

    /**
     * 从Excel文档提取文本内容
     */
    private String extractExcelText(InputStream inputStream) throws Exception {
        org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(inputStream);
        StringBuilder sb = new StringBuilder();
        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
            org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
            if (!sb.isEmpty()) {
                sb.append("\n--- Sheet: ").append(sheet.getSheetName()).append(" ---\n");
            } else {
                sb.append("--- Sheet: ").append(sheet.getSheetName()).append(" ---\n");
            }
            for (org.apache.poi.ss.usermodel.Row row : sheet) {
                boolean firstCell = true;
                for (org.apache.poi.ss.usermodel.Cell cell : row) {
                    if (!firstCell) {
                        sb.append("\t");
                    }
                    sb.append(getCellText(cell));
                    firstCell = false;
                }
                sb.append("\n");
            }
        }
        workbook.close();
        return sb.toString();
    }

    /**
     * 获取单元格文本
     */
    private String getCellText(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    // ==================== 文件操作 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean moveFile(MoveFileRequest request) {
        File file = getFileByIdOrThrow(request.getFileId());
        validateTargetFolder(request.getTargetParentId());
        if (checkFileNameExists(request.getBucketName(), request.getTargetParentId(), file.getFileName(), file.getIsFolder()) > 0
                && !file.getId().equals(request.getFileId())) {
            throw new BusinessException("目标目录下已存在同名文件");
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
            throw new BusinessException("不支持复制文件夹");
        }
        validateTargetFolder(request.getTargetParentId());
        if (checkFileNameExists(request.getBucketName(), request.getTargetParentId(), file.getFileName(), false) > 0) {
            throw new BusinessException("目标目录下已存在同名文件: " + file.getFileName());
        }
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String newFilePath = generateFilePathWithTimestamp(file.getFileName(), timestamp);
            File newFile = createFileEntity(request.getBucketName(), request.getTargetParentId(),
                    file.getFileName(), newFilePath, file.getFileSize(), file.getContentType(), false);
            fileMapper.insert(newFile);
            MinioUtil.copyObject(request.getBucketName(), file.getFilePath(), newFilePath);
            log.info("复制文件成功: fileId={}, newFileId={}, targetParentId={}, newFilePath={}", request.getFileId(), newFile.getId(), request.getTargetParentId(), newFilePath);
            return newFile.getId();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("复制文件失败: fileId={}", request.getFileId(), e);
            throw new BusinessException("复制文件失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean renameFile(RenameFileRequest request) {
        File file = getFileByIdOrThrow(request.getFileId());
        if (checkFileNameExists(request.getBucketName(), file.getParentId(), request.getNewName(), file.getIsFolder()) > 0) {
            throw new BusinessException("同级目录下已存在同名文件");
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
                        throw new BusinessException("文件夹 " + file.getFileName() + " 不为空，无法删除");
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
        return convertToFileResponse(file);
    }

    // ==================== 私有方法 ====================

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
                throw new BusinessException("目标文件夹不存在");
            }
        }
    }

    /**
     * 根据ID获取文件，不存在则抛异常
     */
    private File getFileByIdOrThrow(Long fileId) {
        File file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new BusinessException("文件不存在");
        }
        return file;
    }

    /**
     * 创建文件实体
     */
    private File createFileEntity(String bucketName, Long parentId, String fileName, String filePath,
                                  Long fileSize, String contentType, Boolean isFolder) {
        File fileEntity = new File();
        fileEntity.setBucketName(bucketName);
        fileEntity.setParentId(parentId);
        fileEntity.setFileName(fileName);
        fileEntity.setFilePath(filePath);
        fileEntity.setFileSize(fileSize);
        fileEntity.setContentType(contentType);
        fileEntity.setIsFolder(isFolder);
        fileEntity.setUserId(getCurrentUserId());
        return fileEntity;
    }

    /**
     * 转换File实体为FileResponse
     */
    private FileResponse convertToFileResponse(File file) {
        String username = null;
        if (file.getUserId() != null) {
            User user = userMapper.selectById(file.getUserId());
            if (user != null) {
                username = user.getUserName();
            }
        }
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
     * 生成新的文件路径
     * @deprecated 已废弃，现在使用 {@link #generateFilePathWithTimestamp(String, String)}
     */
    @Deprecated
    private String generateNewFilePath(String bucketName, String originalPath, String originalName, Long targetParentId) {
        int lastDotIndex = originalName.lastIndexOf('.');
        String name = lastDotIndex > 0 ? originalName.substring(0, lastDotIndex) : originalName;
        String extension = lastDotIndex > 0 ? originalName.substring(lastDotIndex) : "";
        String newName = name + "_copy" + extension;
        int counter = 1;
        String tempName = newName;
        while (checkFileNameExists(bucketName, targetParentId, tempName, false) > 0) {
            tempName = name + "_copy_" + counter + extension;
            counter++;
        }
        return tempName;
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
            throw new BusinessException("用户未登录");
        }
        return user.getId();
    }
}