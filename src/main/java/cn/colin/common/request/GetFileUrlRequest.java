package cn.colin.common.request;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GetFileUrlRequest {
    @Parameter(description = "存储桶名称", required = true)
    @NotBlank(message = "存储桶名称不能为空")
    private String bucketName;

    @Parameter(description = "文件ID", required = true)
    @NotNull(message = "文件ID不能为空")
    private String fileId;
}
