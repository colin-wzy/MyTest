package cn.colin.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 获取文件列表请求
 *
 * @author Administrator
 */
@Data
@Schema(description = "获取文件列表请求")
public class GetFileListRequest {
    @Schema(description = "存储桶名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bucketName;

    @Schema(description = "文件夹ID，0表示根目录")
    private Long parentId;

    @Schema(description = "搜索关键字（可选）")
    private String searchKeyword;

    @Schema(description = "页码，从1开始")
    @Positive(message = "页码必须为正数")
    private Integer pageNum;

    @Schema(description = "每页大小")
    @Positive(message = "每页大小必须为正数")
    private Integer pageSize;

    @Schema(description = "是否只查询文件夹")
    private Boolean folderOnly;
}