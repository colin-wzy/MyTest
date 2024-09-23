package cn.colin.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Administrator
 */
public class TokenUtil {
    private static final Map<String, String> USER_TOKEN_MAP = new ConcurrentHashMap<>();

    public static void putToken(String userName, String token) {
        USER_TOKEN_MAP.put(userName, token);
    }

    public static String getToken(String userName) {
        return USER_TOKEN_MAP.get(userName);
    }

    public static void removeToken(String userName) {
        USER_TOKEN_MAP.remove(userName);
    }
}
