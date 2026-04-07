package cn.colin.common.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 删除文件请求
 *
 * @author Administrator
 */
@Data
public class DeleteFileRequest {
    @NotBlank(message = "存储桶名称不能为空")
    private String bucketName;

    @NotBlank(message = "文件ID不能为空")
    private String fileId;
}