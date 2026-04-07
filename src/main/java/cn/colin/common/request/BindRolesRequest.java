package cn.colin.common.request;

import lombok.Data;
import java.util.List;

/**
 * @author Administrator
 */
@Data
public class BindRolesRequest {
    private Long userId;
    private List<Long> roleIds;
}