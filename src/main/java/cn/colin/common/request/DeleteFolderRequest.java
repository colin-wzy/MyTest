package cn.colin.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 删除文件夹请求
 *
 * @author Administrator
 */
@Data
@Schema(description = "删除文件夹请求")
public class DeleteFolderRequest {
    @Schema(description = "存储桶名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "存储桶名称不能为空")
    private String bucketName;

    @Schema(description = "文件夹ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "文件夹ID不能为空")
    @Positive(message = "文件夹ID必须为正数")
    private Long folderId;
}