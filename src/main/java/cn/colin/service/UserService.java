package cn.colin.service;

import cn.colin.entity.User;
import cn.colin.request.LoginRequest;

/**
 * @author Administrator
 */
public interface UserService {
    String login(LoginRequest request);

    void logout();

    User findUser(String userId);

    User findCurrentUser();

    void addUser(User user);
}
