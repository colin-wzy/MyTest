package cn.colin.controller;

import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import cn.colin.entity.User;
import cn.colin.request.LoginRequest;
import cn.colin.service.UserService;
import cn.colin.common.Response;


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

    @PostMapping("/logout")
    public Response<String> logout() {
        userService.logout();
        return Response.success();
    }

    @PostMapping("/findUserById/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Cacheable(value = "user", key = "#userId")
//    @PreAuthorize("authentication.name == 'test'")
    public Response<User> findUserById(@PathVariable String userId) {
        return Response.success(userService.findUser(userId));
    }

    @PostMapping("/findCurrentUser")
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
