package com.blog.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {

    private Integer code; // 0 或 200 表示成功
    private String msg;
    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return success(200, "success", data);
    }
    
    public static <T> Result<T> success(String message) {
        return success(200, message, null);
    }

    public static <T> Result<T> success(Integer code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message, null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return error(code, message, null);
    }

    public static <T> Result<T> error(Integer code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(message);
        result.setData(data);
        return result;
    }

}
