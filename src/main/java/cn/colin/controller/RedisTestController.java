package cn.colin.controller;

import cn.colin.common.response.Response;
import cn.colin.limit.RateLimited;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.colin.service.RedisTestService;


/**
 * @author Administrator
 */
@RestController
@RequestMapping(value = "/redisTest")
public class RedisTestController {

    @Resource
    private RedisTestService redisTestService;

    @GetMapping("/hello")
    @RateLimited(2.0)
    public Response<String> hello() {
        return Response.success("hello world");
    }

    @GetMapping("/helloRedis")
    public String helloRedis() {
        try {
            redisTestService.addInRedis();
            return redisTestService.getInRedis();
        } finally {
            redisTestService.deleteInRedis();
        }
    }

}
