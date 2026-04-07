package cn.colin.common.request;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SearchFileRequest {
    @Parameter(description = "存储桶名称", required = true)
    @NotBlank(message = "存储桶名称不能为空")
    private String bucketName;

    @Parameter(description = "搜索关键字", required = true)
    @NotBlank(message = "搜索关键字不能为空")
    private String keyword;

    @Parameter(description = "页码")
    @Positive(message = "页码必须为正数")
    private Integer pageNum = 1;

    @Parameter(description = "每页大小")
    @Positive(message = "每页大小必须为正数")
    private Integer pageSize = 20;
}
