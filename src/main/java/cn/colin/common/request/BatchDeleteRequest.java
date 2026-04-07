package cn.colin.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量删除请求
 *
 * @author Administrator
 */
@Data
@Schema(description = "批量删除请求")
public class BatchDeleteRequest {
    @Schema(description = "存储桶名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "存储桶名称不能为空")
    private String bucketName;

    @Schema(description = "文件ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "文件ID列表不能为空")
    private List<Long> fileIds;
}