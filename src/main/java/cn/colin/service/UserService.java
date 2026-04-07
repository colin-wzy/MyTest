package cn.colin.service;

import cn.colin.common.entity.Role;
import cn.colin.common.entity.User;
import cn.colin.common.request.LoginRequest;
import cn.colin.common.request.UpdateUserRequest;
import java.util.List;

/**
 * @author Administrator
 */
public interface UserService {
    String login(LoginRequest request);

    String refreshToken(String token);

    void logout();

    User findUserById(Long userId);

    List<User> findUserByName(String userName);

    User findCurrentUser();

    List<User> findAllUsers();

    void addUser(User user);

    void deleteUserById(Long userId);

    void updateUser(UpdateUserRequest request);

    void testTransactional();

    void bindRoles(Long userId, List<Long> roleIds);

    List<Role> findUserRoles(Long userId);

    List<Role> findCurrentUserRoles();
}
