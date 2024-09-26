package cn.colin.service;

import cn.colin.common.entity.User;
import cn.colin.common.request.LoginRequest;

/**
 * @author Administrator
 */
public interface UserService {
    String login(LoginRequest request);

    String refreshToken(String token);

    void logout();

    User findUserById(String userId);

    User findUserByName(String userName);

    User findCurrentUser();

    void addUser(User user);

    void deleteUserById(String userId);

    void testTransactional();
}
