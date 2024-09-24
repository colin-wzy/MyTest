package cn.colin.common.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Administrator
 */
@Data
public class Response<T> implements Serializable {
    private static final String SUCCESS_CODE = "000000";
    private static final String SUCCESS_MSG = "success";

    public static final String FAILED_CODE = "999999";
    private static final String FAILED_MSG = "failed";

    /**
     * 业务状态码
     */
    private String code;
    /**
     * 信息
     */
    private String msg;
    /**
     * 返回的数据体
     */
    private T data;

    public static <T> Response<T> success(T data) {
        Response<T> body = new Response<>();
        body.setCode(SUCCESS_CODE);
        body.setMsg(SUCCESS_MSG);
        body.setData(data);
        return body;
    }

    public static <T> Response<T> failed(String code, String errorMsg) {
        Response<T> body = new Response<>();
        body.setCode(code);
        body.setMsg(errorMsg);
        return body;
    }

    public static <T> Response<T> success() {
        return success(null);
    }

    public static <T> Response<T> failed() {
        return failed(FAILED_CODE, FAILED_MSG);
    }

    public static <T> Response<T> failed(String errorMsg) {
        return failed(FAILED_CODE, errorMsg);
    }
}
