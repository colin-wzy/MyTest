package cn.colin.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;
import java.util.UUID;

/**
 * @author Administrator
 */
public class JwtUtil {
    private final static String SIGNATURE = "signature";
    private final static long DEFAULT_EXPIRE_TIME = 12 * 60 * 60L;

    public static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String createJwt(String userName) {
        return createJwt(userName, DEFAULT_EXPIRE_TIME);
    }

    public static String createJwt(String userName, long expireSecondTime) {
        JWTCreator.Builder builder = JWT.create();
        builder.withSubject(userName);
        long expireTime = System.currentTimeMillis() + (expireSecondTime * 1000);
        builder.withExpiresAt(new Date(expireTime));
        return builder.sign(Algorithm.HMAC256(SIGNATURE));
    }

    public static DecodedJWT verify(String token) {
        return JWT.require(Algorithm.HMAC256(SIGNATURE)).build().verify(token);
    }
}
