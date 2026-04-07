package cn.colin.controller;

import cn.colin.common.response.Response;
import cn.colin.limit.RateLimited;
import jakarta.annotation.Resource;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.colin.service.RedisTestService;

import java.util.Collection;


/**
 * @author Administrator
 */
@RestController
@RequestMapping(value = "/redisTest")
public class RedisTestController {

    @Resource
    private RedisTestService redisTestService;
    @Resource
    private CacheManager cacheManager;

    @PostMapping("/hello")
    @RateLimited(2.0)
    public Response<String> hello() {
        return Response.success("hello world");
    }

    @PostMapping("/helloRedis")
    public String helloRedis() {
        try {
            redisTestService.addInRedis();
            return redisTestService.getInRedis();
        } finally {
            redisTestService.deleteInRedis();
        }
    }

    @PostMapping("/cacheManagerNames")
    public Response<Collection<String>> cacheManagerNames() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        return Response.success(cacheNames);
    }

}
