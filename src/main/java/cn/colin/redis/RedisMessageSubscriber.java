package cn.colin.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 */
@Slf4j
@Service
public class RedisMessageSubscriber {

    public void handlerMessage(String message) {
        log.info("Received user operation: {}", message);
    }
}
