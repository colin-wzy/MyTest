package cn.colin.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 移动文件请求
 *
 * @author Administrator
 */
@Data
@Schema(description = "移动文件请求")
public class MoveFileRequest {
    @Schema(description = "存储桶名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "存储桶名称不能为空")
    private String bucketName;

    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "文件ID不能为空")
    @Positive(message = "文件ID必须为正数")
    private Long fileId;

    @Schema(description = "目标文件夹ID，0表示根目录")
    @NotNull(message = "目标文件夹ID不能为空")
    private Long targetParentId;
}