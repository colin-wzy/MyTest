package cn.colin.service.impl;

import cn.colin.common.entity.Role;
import cn.colin.common.entity.UserRole;
import cn.colin.mapper.RoleMapper;
import cn.colin.mapper.UserRoleMapper;
import cn.colin.service.RoleService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private UserRoleMapper userRoleMapper;

    @Override
    public Role addRole(Role role) {
        roleMapper.insert(role);
        return role;
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        roleMapper.deleteById(roleId);
        // 删除用户角色关联
        userRoleMapper.delete(Wrappers.lambdaQuery(UserRole.class).eq(UserRole::getRoleId, roleId));
    }

    @Override
    public void updateRole(Role role) {
        roleMapper.updateById(role);
    }

    @Override
    public Role findRoleById(Long roleId) {
        return roleMapper.selectById(roleId);
    }

    @Override
    public List<Role> findAllRoles() {
        return roleMapper.selectList(Wrappers.lambdaQuery(Role.class));
    }

    @Override
    public List<Role> findRolesByUserId(Long userId) {
        List<UserRole> userRoles = userRoleMapper.selectList(
                Wrappers.lambdaQuery(UserRole.class).eq(UserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return List.of();
        }
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        return roleMapper.selectList(Wrappers.lambdaQuery(Role.class).in(Role::getRoleId, roleIds));
    }

    @Override
    @Transactional
    public void bindRoles(Long userId, List<Long> roleIds) {
        // 先删除用户原有角色
        userRoleMapper.delete(Wrappers.lambdaQuery(UserRole.class).eq(UserRole::getUserId, userId));
        // 插入新角色
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }
}