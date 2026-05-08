package cn.colin.exceptions;

import lombok.Getter;

import static cn.colin.common.response.Response.FILE_FAILED_CODE;

/**
 * 业务异常类
 *
 * @author Administrator
 */
@Getter
public class FileServiceException extends RuntimeException {
    private final String code;

    public FileServiceException(String message) {
        super(message);
        this.code = FILE_FAILED_CODE;
    }
}