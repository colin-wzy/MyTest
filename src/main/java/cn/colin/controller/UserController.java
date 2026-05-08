package cn.colin.controller;

import cn.colin.limit.RateLimited;
import cn.colin.log.LoggingOperation;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import cn.colin.common.entity.User;
import cn.colin.common.request.FindUserByIdRequest;
import cn.colin.common.request.FindUserByNameRequest;
import cn.colin.common.request.LoginRequest;
import cn.colin.common.request.RefreshTokenRequest;
import cn.colin.common.request.BindRolesRequest;
import cn.colin.common.request.FindUserRolesRequest;
import cn.colin.common.request.UpdateUserRequest;
import cn.colin.service.UserService;
import cn.colin.common.response.Response;
import cn.colin.common.entity.Role;
import java.util.List;


/**
 * @author Administrator
 */
@RestController
@RequestMapping(value = "/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/login")
    public Response<String> login(@RequestBody LoginRequest request) {
        return Response.success(userService.login(request));
    }

    @PostMapping("/refreshToken")
    public Response<String> refreshToken(@RequestBody RefreshTokenRequest request) {
        return Response.success(userService.refreshToken(request.getToken()));
    }

    @PostMapping("/logout")
    public Response<String> logout() {
        userService.logout();
        return Response.success();
    }

    @PostMapping("/findUserById")
    @Cacheable(value = "userId", key = "#request.userId")
    public Response<User> findUserById(@RequestBody FindUserByIdRequest request) {
        return Response.success(userService.findUserById(request.getUserId()));
    }

    @PostMapping("/findUserByName")
    public Response<List<User>> findUserByName(@RequestBody FindUserByNameRequest request) {
        return Response.success(userService.findUserByName(request.getUserName()));
    }

    @PostMapping("/findCurrentUser")
    @RateLimited()
    @LoggingOperation
    public Response<User> findCurrentUser() {
        return Response.success(userService.findCurrentUser());
    }

    @PostMapping("/findAllUsers")
    public Response<List<User>> findAllUsers() {
        return Response.success(userService.findAllUsers());
    }

    @PostMapping("/addUser")
    @PreAuthorize("hasRole('ADMIN')")
    public Response<String> addUser(@RequestBody User user) {
        userService.addUser(user);
        return Response.success();
    }

    @PostMapping("/deleteUserById/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "userId", key = "#userId")
    public Response<String> deleteUserById(@PathVariable Long userId) {
        userService.deleteUserById(userId);
        return Response.success();
    }

    @PostMapping("/updateUser")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "userId", key = "#request.id")
    public Response<String> updateUser(@RequestBody UpdateUserRequest request) {
        userService.updateUser(request);
        return Response.success();
    }

    @PostMapping("/testTransactional")
    public Response<Void> testTransactional() {
        userService.testTransactional();
        return Response.success();
    }

    @PostMapping("/bindRoles")
    @PreAuthorize("hasRole('ADMIN')")
    public Response<Boolean> bindRoles(@RequestBody BindRolesRequest request) {
        userService.bindRoles(request.getUserId(), request.getRoleIds());
        return Response.success(true);
    }

    @PostMapping("/findUserRoles")
    public Response<List<Role>> findUserRoles(@RequestBody FindUserRolesRequest request) {
        return Response.success(userService.findUserRoles(request.getUserId()));
    }

    @PostMapping("/findCurrentUserRoles")
    public Response<List<Role>> findCurrentUserRoles() {
        return Response.success(userService.findCurrentUserRoles());
    }
}
