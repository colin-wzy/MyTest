package cn.colin.common.office;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "OnlyOffice JS API 编辑器配置")
public class OnlyOfficeConfig {

    @Schema(description = "文档信息")
    private Document document;

    @Schema(description = "编辑器配置")
    private EditorConfig editorConfig;

    @Schema(description = "编辑器高度", defaultValue = "100%")
    @Builder.Default
    private String height = "100%";

    @Schema(description = "编辑器宽度", defaultValue = "100%")
    @Builder.Default
    private String width = "100%";

    @Schema(description = "编辑器类型", defaultValue = "desktop")
    @Builder.Default
    private String type = "desktop";

    @Schema(description = "文档类型分类: word / cell / slide / pdf")
    private String documentType;

    @Schema(description = "OnlyOffice Document Server 地址，前端用于加载 JS API")
    private String docServerUrl;

    // ============ 内部类 ============

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "OnlyOffice 文档信息")
    public static class Document {
        @Schema(description = "文件类型: word/cell/slide/pdf/text")
        private String fileType;

        @Schema(description = "文档唯一标识，用于缓存和版本控制")
        private String key;

        @Schema(description = "文档标题（文件名）")
        private String title;

        @Schema(description = "文档下载URL，OnlyOffice服务器通过此地址获取文件")
        private String url;

        @Schema(description = "文档编辑权限")
        private Permissions permissions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "OnlyOffice 编辑器配置")
    public static class EditorConfig {
        @Schema(description = "保存回调URL，编辑后OnlyOffice回调此地址")
        private String callbackUrl;

        @Schema(description = "编辑器模式: view(预览) / edit(编辑)")
        private String mode;

        @Schema(description = "界面语言", defaultValue = "zh-CN")
        @Builder.Default
        private String lang = "zh-CN";

        @Schema(description = "当前用户信息")
        private User user;

        @Schema(description = "编辑器自定义配置")
        private Customization customization;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "OnlyOffice 编辑器自定义配置")
    public static class Customization {
        @Schema(description = "关闭时强制保存")
        private Boolean forcesave;

        @Schema(description = "启用自动保存")
        private Boolean autosave;

        @Schema(description = "隐藏右侧菜单")
        private Boolean compactHeader;

        @Schema(description = "隐藏工具栏标签")
        private Boolean compactToolbar;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "OnlyOffice 文档权限配置")
    public static class Permissions {
        @Schema(description = "允许编辑")
        private Boolean edit;

        @Schema(description = "允许下载")
        private Boolean download;

        @Schema(description = "允许打印")
        private Boolean print;

        @Schema(description = "允许审阅模式")
        private Boolean review;

        @Schema(description = "允许评论")
        private Boolean comment;

        @Schema(description = "允许填写表单")
        private Boolean fillForms;

        @Schema(description = "允许修改过滤条件")
        private Boolean modifyFilter;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "OnlyOffice 用户信息")
    public static class User {
        @Schema(description = "用户ID")
        private String id;

        @Schema(description = "用户名称")
        private String name;
    }
}
