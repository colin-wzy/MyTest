package cn.colin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.security.Principal;
import java.util.Date;

/**
 * @author admin
 */
@Data
@TableName(value = "`user`")
public class User implements Serializable, Principal {
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户名
     */
    @TableField(value = "user_name")
    private String userName;

    /**
     * 姓名
     */
    @TableField(value = "real_name")
    private String realName;

    /**
     * 密码
     */
    @TableField(value = "pwd")
    private String pwd;

    /**
     * 性别
     */
    @TableField(value = "sex")
    private Boolean sex;

    @TableField(value = "create_time")
    private Date createTime;

    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(value = "d_flag")
    private Boolean dFlag;

    @Override
    @JsonIgnore
    public String getName() {
        //TODO 继承Principal是为了使用@PreAuthorize("authentication.name == 'test'")进行鉴权
        return userName;
    }
}
