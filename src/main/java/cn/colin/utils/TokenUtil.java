package cn.colin.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Administrator
 */
public class TokenUtil {
    private static final Map<String, String> TOKEN_MAP = new ConcurrentHashMap<>();

    public static void putToken(String userName, String token) {
        TOKEN_MAP.put(userName, token);
    }

    public static String getToken(String userName) {
        return TOKEN_MAP.get(userName);
    }

    public static void removeToken(String userName) {
        TOKEN_MAP.remove(userName);
    }
}
