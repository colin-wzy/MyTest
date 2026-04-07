package cn.colin.common.request;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GetFolderListRequest {
    @Parameter(description = "存储桶名称", required = true)
    @NotBlank(message = "存储桶名称不能为空")
    private String bucketName;

    @Parameter(description = "父文件夹ID，0表示根目录")
    private Long parentId = 0L;
}
