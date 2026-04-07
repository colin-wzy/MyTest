package cn.colin.service;

import cn.colin.common.entity.Role;
import java.util.List;

/**
 * @author Administrator
 */
public interface RoleService {
    Role addRole(Role role);

    void deleteRole(Long roleId);

    void updateRole(Role role);

    Role findRoleById(Long roleId);

    List<Role> findAllRoles();

    List<Role> findRolesByUserId(Long userId);

    void bindRoles(Long userId, List<Long> roleIds);
}