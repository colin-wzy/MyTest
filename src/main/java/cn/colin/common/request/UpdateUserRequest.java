package cn.colin.common.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateUserRequest implements Serializable {
    private Long id;
    private String userName;
    private String realName;
    private String pwd;
    private Boolean sex;
}