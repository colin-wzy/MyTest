package cn.colin.controller;

import cn.colin.common.entity.Role;
import cn.colin.common.request.FindRolesByUserIdRequest;
import cn.colin.service.RoleService;
import cn.colin.common.response.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * @author Administrator
 */
@Slf4j
@RestController
@RequestMapping(value = "/role")
public class RoleController {
    @Resource
    private RoleService roleService;

    @PostMapping("/addRole")
    public Response<Role> addRole(@RequestBody Role role) {
        return Response.success(roleService.addRole(role));
    }

    @PostMapping("/deleteRole/{roleId}")
    public Response<Boolean> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return Response.success(true);
    }

    @PostMapping("/updateRole")
    public Response<Boolean> updateRole(@RequestBody Role role) {
        roleService.updateRole(role);
        return Response.success(true);
    }

    @PostMapping("/findRoleById")
    public Response<Role> findRoleById(@RequestBody Role role) {
        return Response.success(roleService.findRoleById(role.getRoleId()));
    }

    @PostMapping("/findAllRoles")
    public Response<List<Role>> findAllRoles() {
        return Response.success(roleService.findAllRoles());
    }

    @PostMapping("/findRolesByUserId")
    public Response<List<Role>> findRolesByUserId(@RequestBody FindRolesByUserIdRequest request) {
        return Response.success(roleService.findRolesByUserId(request.getUserId()));
    }
}