package cn.colin.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 文件响应DTO
 *
 * @author Administrator
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件响应信息")
public class FileResponse {
    @Schema(description = "文件ID")
    private Long id;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件类型/MIME类型")
    private String contentType;

    @Schema(description = "存储桶名称")
    private String bucketName;

    @Schema(description = "父文件夹ID")
    private Long parentId;

    @Schema(description = "是否文件夹")
    private Boolean isFolder;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "创建者用户名")
    private String username;
}