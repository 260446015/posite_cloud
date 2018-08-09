package com.zkjl.posite_cloud.common;

import java.io.Serializable;

public class ApiResult implements Serializable {
    private static final long serialVersionUID = 8052566108151898248L;
    public static final String SUCCESS_MESSAGE_CODE = "front.common.success";
    public static final int SUCCESS = 0;
    public static final int ERROR = -1;
    public static final int LOGIN_ERROR = -3;
    public static final int SYSTEM_ERROR = -4;
    private int code = 0;
    private String message = "";
    private Object data;

    public int getCode() {
        return this.code;
    }

    public ApiResult setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return this.message;
    }

    public ApiResult setMessage(String message) {
        this.message = message;
        return this;
    }

    public <T> T getData() {
        return (T) this.data;
    }

    public ApiResult setData(Object data) {
        this.data = data;
        return this;
    }
}


