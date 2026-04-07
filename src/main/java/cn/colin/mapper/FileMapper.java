package cn.colin.mapper;

import cn.colin.common.entity.File;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件Mapper
 *
 * @author Administrator
 */
@Mapper
public interface FileMapper extends BaseMapper<File> {

    /**
     * 根据存储桶名称和父文件夹ID查询文件列表
     */
    List<File> selectFileListByParentId(@Param("bucketName") String bucketName, @Param("parentId") Long parentId);

    /**
     * 根据存储桶名称和搜索关键字查询文件列表
     */
    List<File> selectFileListByKeyword(@Param("bucketName") String bucketName, @Param("parentId") Long parentId, @Param("keyword") String keyword);

    /**
     * 查询文件夹列表
     */
    List<File> selectFolderList(@Param("bucketName") String bucketName, @Param("parentId") Long parentId);

    /**
     * 根据文件ID删除（物理删除）
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据文件ID列表批量删除
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 移动文件到目标文件夹
     */
    int moveFile(@Param("fileId") Long fileId, @Param("targetParentId") Long targetParentId);

    /**
     * 检查同级目录下是否存在同名文件
     */
    Long checkFileNameExists(@Param("bucketName") String bucketName, @Param("parentId") Long parentId, @Param("fileName") String fileName);

    /**
     * 获取文件夹下的子文件/文件夹数量
     */
    Long countChildren(@Param("bucketName") String bucketName, @Param("parentId") Long parentId);
}