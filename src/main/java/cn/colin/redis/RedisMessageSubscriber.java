package cn.colin.redis;

import org.springframework.stereotype.Service;

/**
 * @author Administrator
 */
@Service
public class RedisMessageSubscriber {

    public void handlerMessage(String message) {
        System.out.println("订阅到redis的消息：" + message);
    }
}
