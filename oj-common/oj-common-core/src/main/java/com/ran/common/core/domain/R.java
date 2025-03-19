package com.ran.common.core.domain;

import com.ran.common.core.enums.ResultCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class R<T> {

    private int code;

    private String msg;

    private T data;

    public static <T> R<T> ok() {
        return assembleResult(ResultCode.SUCCESS, null);
    }

    public static <T> R<T> ok(T data) {
        return assembleResult(ResultCode.SUCCESS, data);
    }

    public static <T> R<T> fail() {
        return assembleResult(ResultCode.FAILED, null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return assembleResult(code, msg, null);
    }

    public static <T> R<T> fail(ResultCode resultCode) {
        return assembleResult(resultCode, null);
    }

    private static <T> R<T> assembleResult(ResultCode resultCode, T data) {
        R<T> r = new R<>();
        r.setCode(resultCode.getCode());
        r.setData(data);
        r.setMsg(resultCode.getMsg());
        return r;
    }

    private static <T> R<T> assembleResult(int code, String msg, T data) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setData(data);
        r.setMsg(msg);
        return r;
    }
}
