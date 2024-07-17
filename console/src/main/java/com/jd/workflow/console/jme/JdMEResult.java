package com.jd.workflow.console.jme;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 京东ME请求结果
 * @author xiaobei
 * @date 2022-12-21 20:12
 */
@Getter
@Setter
@ToString
public class JdMEResult<T> {

    private Integer code;

    private String msg;

    private T data;

    private Boolean success;

    public static <T> JdMEResult<T> error(Integer code) {
        JdMEResult<T> result = new JdMEResult<>();
        result.setCode(code);
        result.setMsg(JdMECodeEnum.getReasonByCode(code));
        result.setSuccess(Boolean.FALSE);
        return result;
    }

    public static <T> JdMEResult<T> error(Integer code, String msg) {
        JdMEResult<T> result = new JdMEResult<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setSuccess(Boolean.FALSE);
        return result;
    }
}
