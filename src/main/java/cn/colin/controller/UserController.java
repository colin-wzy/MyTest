package cn.colin.controller;

import cn.colin.limit.RateLimited;
import cn.colin.log.LoggingOperation;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import cn.colin.common.entity.User;
import cn.colin.common.request.LoginRequest;
import cn.colin.service.UserService;
import cn.colin.common.response.Response;


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
    public Response<String> refreshToken(@RequestParam String token) {
        return Response.success(userService.refreshToken(token));
    }

    @PostMapping("/logout")
    public Response<String> logout() {
        userService.logout();
        return Response.success();
    }

    @PostMapping("/findUserById/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "user", key = "#userId")
//    @PreAuthorize("authentication.name == 'test'")
    public Response<User> findUserById(@PathVariable String userId) {
        return Response.success(userService.findUserById(userId));
    }

    @PostMapping("/findUserByName")
    @Cacheable(value = "user", key = "#userName")
    public Response<User> findUserByName(@RequestParam String userName) {
        return Response.success(userService.findUserByName(userName));
    }

    @PostMapping("/findCurrentUser")
    @RateLimited()
    @LoggingOperation
    public Response<User> findCurrentUser() {
        return Response.success(userService.findCurrentUser());
    }

    @PostMapping("/addUser")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Response<String> addUser(@RequestBody User user) {
        userService.addUser(user);
        return Response.success();
    }
}
