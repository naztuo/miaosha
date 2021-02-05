package com.naztuo.common.resultbean;

import com.naztuo.common.enums.ResultStatus;

import java.io.Serializable;

public class RspMsg<T> extends AbstractResult implements Serializable {
    private static final long serialVersionUID = 867933019328199779L;

    private T data;
    private Integer count;

    protected RspMsg(ResultStatus status, String message) {
        super(status, message);
    }

    protected RspMsg(ResultStatus status) {
        super(status);
    }


    protected RspMsg(ResultStatus status, String message, T data, Integer count) {
        super(status, message);
        this.data = data;
        this.count = count;
    }

    protected RspMsg(ResultStatus status, T data) {
        super(status);
        this.data = data;
    }



    public static <T> RspMsg<T> success() {
        return new RspMsg<>(ResultStatus.SUCCESS, null);
    }

    public static <T> RspMsg<T> success(String message) {
        return new RspMsg(ResultStatus.SUCCESS, message);
    }

    public static <T> RspMsg<T> success(T data) {
        return new RspMsg<>(ResultStatus.SUCCESS, data);
    }

    public static <T> RspMsg<T> error(ResultStatus status) {
        return new RspMsg<T>(status);
    }


    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Integer getCount() {
        return this.count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }


}
