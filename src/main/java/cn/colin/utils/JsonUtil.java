package cn.colin.utils;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;


/**
 * @author Administrator
 */
@Component
public class JsonUtil extends JSONUtil {

    private static ObjectMapper objectMapper;

    @Resource
    public void setObjectMapper(ObjectMapper objectMapper) {
        JsonUtil.objectMapper = objectMapper;
    }

    @SneakyThrows
    public static String toJsonString(Object data) {
        if (data == null) {
            return null;
        }
        return data instanceof String ? (String) data : objectMapper.writeValueAsString(data);
    }

    @SneakyThrows
    public static <T> T fromJsonString(String jsonData, Class<T> clazz) {
        if (StringUtils.isEmpty(jsonData) || clazz == null) {
            return null;
        }
        return objectMapper.readValue(jsonData, clazz);
    }
}
