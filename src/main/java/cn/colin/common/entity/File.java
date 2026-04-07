package cn.colin.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件实体类
 *
 * @author Administrator
 */
@Data
@TableName(value = "`sys_file`")
public class File implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文件名称
     */
    @TableField(value = "file_name")
    private String fileName;

    /**
     * 文件路径（MinIO对象名称）
     */
    @TableField(value = "file_path")
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @TableField(value = "file_size")
    private Long fileSize;

    /**
     * 文件类型/MIME类型
     */
    @TableField(value = "content_type")
    private String contentType;

    /**
     * 存储桶名称
     */
    @TableField(value = "bucket_name")
    private String bucketName;

    /**
     * 父文件夹ID，0表示根目录
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 是否文件夹 1是 0否
     */
    @TableField(value = "is_folder")
    private Boolean isFolder;

    /**
     * 上传用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;
}