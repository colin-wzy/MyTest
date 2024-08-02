package cn.colin.request;

import lombok.Data;

/**
 * @author Administrator
 */
@Data
public class LoginRequest {
    private String userName;
    private String pwd;
}
