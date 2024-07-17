package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @description:
 * @date: 2022/5/11 18:16
 * @author wubaizhao1
 */
@AllArgsConstructor
public enum ServiceErrorEnum {

    /**
     *
     */
    SUCCESS(0,"成功"),
    CONTROLLER_BUSINESS(1, "业务控制"),
    COMMON_EXCEPTION(9999, "%s"),

    //=================================500XX公共异常
    SERVICE_INVALID_PARAMETER(50001, "方法参数错误"),
    INSERT_DB_ERROR(50002, "入库失败"),
    INVOKE_PRC_ERROR(50003, "调用rpc失败"),
    UPDATE_DB_ERROR(50004, "修改库失败"),
    SERVER_EXCEPTION(50005, "服务异常，请联系管理员！"),
    TIME_OUT_EXCEPTION(50006, "超时！"),
    TOCKEN_ERROR(50007, "token错误"),
    ERP_ERROR(50008, "erp错误"),
    DATA_DUPLICATION_ERROR(50009, "数据重复错误"),
    DATA_DUPLICATION_ERROR_INFO(50009, "数据重复错误[%s]"),
    INVALID_PARAMETER(50010, "参数错误[%s]"),
    DATA_EMPTY_ERROR(50011, "数据为空"),
    DATA_REPEAT_ERROR(50012, "数据重复[%s]"),
    NO_OPERATION_PERMISSION(50013,"没有权限进行该操作！"),
    PIN_NOT_IN_WHITE_LIST(50014,"登录的账号不在白名单中，请联系管理员处理！"),
    HTTP_INVOKE_ERROR(50015,"调用HTTP方法异常!执行耗时:%s(请检查网络是否通达、处理超时等)"),
    ;



    /**
     *
     */
    @Getter
    @Setter
    private int code;
    /**
     *
     */
    @Getter
    @Setter
    private String msg;


}
