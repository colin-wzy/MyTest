package cn.colin.service;

import cn.colin.common.office.OnlyOfficeConfig;
import cn.colin.common.request.*;
import cn.colin.common.response.FileListResponse;
import cn.colin.common.response.FilePreviewResponse;
import cn.colin.common.response.FileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface FileService {

    /**
     * 批量上传文件
     *
     * @param bucketName 存储桶名称
     * @param parentId   父文件夹ID
     * @param files      文件数组
     * @return 文件ID列表
     */
    List<Long> uploadFiles(String bucketName, Long parentId, MultipartFile[] files);

    /**
     * 下载文件
     *
     * @param bucketName 存储桶名称
     * @param fileName   文件名
     * @return 输入流
     */
    InputStream downloadFile(String bucketName, String fileName);

    // ==================== 文件管理 ====================

    /**
     * 获取文件URL
     *
     * @param bucketName 存储桶名称
     * @param fileName   文件名
     * @return 文件URL
     */
    String getFileUrl(String bucketName, String fileName);

    // ==================== 文件夹管理 ====================

    /**
     * 创建文件夹
     *
     * @param request 创建文件夹请求
     * @return 文件夹ID
     */
    Long createFolder(CreateFolderRequest request);

    /**
     * 删除文件夹
     *
     * @param request 删除文件夹请求
     * @return 是否成功
     */
    boolean deleteFolder(DeleteFolderRequest request);

    /**
     * 获取文件夹列表
     *
     * @param bucketName 存储桶名称
     * @param parentId   父文件夹ID，0表示根目录
     * @return 文件夹列表
     */
    List<FileResponse> getFolderList(String bucketName, Long parentId);

    // ==================== 文件列表 ====================

    /**
     * 获取文件列表
     *
     * @param request 获取文件列表请求
     * @return 文件列表响应
     */
    FileListResponse getFileList(GetFileListRequest request);

    // ==================== 文件预览 ====================

    /**
     * 获取文件预览
     *
     * @param request 获取文件预览请求
     * @return 文件预览响应
     */
    FilePreviewResponse getFilePreview(GetFilePreviewRequest request);

    // ==================== 文件操作 ====================

    /**
     * 移动文件
     *
     * @param request 移动文件请求
     * @return 是否成功
     */
    boolean moveFile(MoveFileRequest request);

    /**
     * 复制文件
     *
     * @param request 复制文件请求
     * @return 新文件ID
     */
    Long copyFile(CopyFileRequest request);

    /**
     * 重命名文件
     *
     * @param request 重命名文件请求
     * @return 是否成功
     */
    boolean renameFile(RenameFileRequest request);

    /**
     * 批量删除文件
     *
     * @param request 批量删除请求
     * @return 删除的文件数量
     */
    int batchDelete(BatchDeleteRequest request);

    /**
     * 根据ID获取文件信息
     *
     * @param fileId 文件ID
     * @return 文件信息
     */
    FileResponse getFileById(Long fileId);

    /**
     * 获取OnlyOffice文档编辑配置
     *
     * @param request 编辑请求
     * @return OnlyOffice配置
     */
    OnlyOfficeConfig getOfficeEditConfig(GetFileEditRequest request);

    /**
     * 处理OnlyOffice保存回调
     *
     * @param body 回调请求体JSON
     * @return 回调响应
     */
    String handleOfficeCallback(String body);
}