package cn.colin.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

/**
 * 创建文件夹请求
 *
 * @author Administrator
 */
@Data
@Schema(description = "创建文件夹请求")
public class CreateFolderRequest {
    @Schema(description = "存储桶名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "存储桶名称不能为空")
    private String bucketName;

    @Schema(description = "文件夹名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文件夹名称不能为空")
    private String folderName;

    @Schema(description = "父文件夹ID，0表示根目录")
    @PositiveOrZero(message = "父文件夹ID必须为非负数")
    private Long parentId;
}