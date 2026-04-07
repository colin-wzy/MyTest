package cn.colin.common.request;

import lombok.Data;

@Data
public class FindRolesByUserIdRequest {
    private Long userId;
}