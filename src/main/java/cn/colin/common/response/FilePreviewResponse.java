package cn.colin.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件预览响应DTO
 *
 * @author Administrator
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件预览响应")
public class FilePreviewResponse {
    @Schema(description = "文件ID")
    private Long fileId;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "预览内容（文本内容或Base64编码的图片）")
    private String content;

    @Schema(description = "文件类型")
    private String contentType;

    @Schema(description = "是否支持预览")
    private Boolean previewable;
}