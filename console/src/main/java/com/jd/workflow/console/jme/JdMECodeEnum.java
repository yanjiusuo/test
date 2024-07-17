package com.jd.workflow.console.jme;

/**
 * 发送京ME消息 http 请求错误码
 * 参见：http://regist-open.timline.jd.com/?code=3y7PAiuVrnRehTyPgAgpyB6-rs7QsA8jWkR9blj4y9Q&state=Cu9h9SDl8-y9c_zr22uSwjuhG5lKJ05x7Rg-s3C9VJ0#/api-list
 * @author xiaobei
 * @date 2022-12-21 19:57
 */
public enum JdMECodeEnum {
    SUCCESS(0 , "成功"),
    GATEWAY_SERVICE_CALL_EXCEPTION(10100001,"网关服务调用异常"),
    ACCESS_CREDENTIALS_ARE_EMPTY(10100002,"访问凭证为空"),
    REQUEST_PARAMETER_ERROR(10100003,"请求参数错误"),
    REPEAT_REQUEST_REJECTED(10100004,"重复请求,已拒绝"),
    FREQUENT_REQUESTS_PLEASE_TRY_AGAIN_LATER(10100005,"请求频繁,请稍后重试"),
    API_MAPPING_NOT_CONFIGURED(10100006,"API映射未配置"),
    SERVICE_INTERFACE_IS_NOT_CONFIGURED(10100007,"服务接口未配置"),
    CALL_JSF_INTERFACE_ERROR(10100008,"调用JSF接口错误"),
    THE_API_INTERFACE_ACCESSED_DOES_NOT_EXIST(10100009,"访问的API接口不存在"),
    THE_REQUEST_METHOD_DOES_NOT_SUPPORT(10100010,"请求方式不支持"),
    NO_SEND_PERMISSION_FOR_THIS_MESSAGE_TYPE(10100011,"无此消息类型的发送权限"),
    ROUTING_SERVICE_ACK_RECEIPT_IS_EMPTY(10100012,"路由服务ack回执为空"),
    SHARING_THE_MESSAGE_IS_SUCCESSFUL_BUT_LEAVING_A_MESSAGE_FAILS(10100013,"分享消息成功,但留言消息失败"),
    THE_BODY_PARAMETER_OF_THE_MESSAGE_CONTENT_IS_WRONG(10100014,"消息内容body参数错误"),
    NO_SEND_PERMISSION_FOR_THIS_PROTOCOL_TYPE(10100015,"无此协议类型的发送权限"),
    REQUEST_ID_CANNOT_BE_EMPTY(10100016,"请求ID不能为空"),
    THE_API_INTERFACE_NEEDS_TO_OPEN_THE_ROBOT_PERMISSION(10100017,"API接口需要开通机器人权限"),
    USER_LOGIN_CREDENTIALS_ARE_EMPTY(10100018,"用户登录凭证为空"),
    SEND_PERMISSION_WITHOUT_CUSTOM_FROM(10100019,"无自定义from的发送权限"),
    INVALID_ROBOT_INFORMATION(10100020,"无效的机器人信息"),
    JMQ_MESSAGE_SENDING_ERROR(10100021,"JMQ消息发送错误"),
    THE_PROTOCOL_DOES_NOT_HAVE_A_JMQ_TOPIC_CONFIGURED(10100021,"协议没有配置JMQ主题"),
    PARAMETER_ERROR(10300000,"参数错误"),
    GROUP_INFORMATION_DOES_NOT_EXIST(10300001,"群信息不存在"),
    NOT_AUTHORIZED_TO_PERFORM_THIS_OPERATION(10300002,"无权限进行该操作"),
    NO_MODIFICATION(10300003,"无修改项"),
    INVITER_IS_NOT_A_GROUP_MEMBER(10300004,"邀请人不是群成员"),
    NO_INVITATION_PERMISSION(10300005,"无邀请权限"),
    EXCEEDED_THE_MAXIMUM_GROUP_LIMIT(10300006,"超过群最大人数限制"),
    GROUP_CREATION_FAILED_DOES_NOT_SUPPORT_THE_CREATION_OF_GROUPS_WITH_MULTIPLE_ACCOUNT_SYSTEMS(10300007,"群创建失败,不支持创建含多个账号体系的群"),
    GROUP_CREATION_FAILED_VALID_GROUP_MEMBER_LIST_IS_EMPTY(10300008,"群创建失败,有效群成员列表为空"),
    THE_NEW_GROUP_OWNER_IS_SET_NOT_A_GROUP_MEMBER(10300009,"设定的新群主,不是群成员"),
    ROBOTS_CANNOT_BE_SET_AS_GROUP_OWNERS(10300010,"机器人不能设置为群主"),
    PARAMETER_ERROR_1(10400000,"参数错误"),
    INTERNAL_ERROR(10400001,"内部错误"),
    NO_DATA_ACCESS(10400002,"无数据访问权限"),
    INTERFACE_IS_NOT_AUTHORIZED(10600001,"接口未授权"),
    PARAMETER_ERROR_2(10600002,"参数错误"),
    APP_DOES_NOT_EXIST(10600003,"APP不存在"),
    INCORRECT_TOKEN(10600004,"Token不正确"),
    TOKEN_HAS_EXPIRED_PLEASE_APPLY_AGAIN(10600005,"Token已经过期，请重新申请"),
    PERMISSION_SYSTEM_INTERNAL_ERROR(10600006,"权限系统内部错误"),
    INCORRECT_RESOURCE_OR_REQUEST_METHOD(10600007,"资源或请求方法不正确"),
    INVALID_AID(10600008,"无效的aid"),
    SSO_LOGIN_CREDENTIALS_VERIFICATION_FAILED(10600109,"SSO登录凭证验证失败"),
    INTERFACE_DATA_PERMISSION_VERIFICATION_FAILED(10600010,"接口数据权限验证失败"),
    PARAMETER_ERROR_3(10800000,"参数错误"),
    INTERNAL_ERROR_1(10800001,"内部错误"),
    PARAMETER_ERROR_4(10900000,"参数错误"),
    INTERNAL_ERROR_2(10900001,"内部错误"),
    NON_ENTERPRISE_DONGDONG_TENANTS_HAVE_NO_REQUEST_PERMISSION(10900002,"非企业咚咚租户无请求权限"),
    NO_DATA_ACCESS_2(10900003,"无数据访问权限"),
    REQUEST_INFORMATION_DOES_NOT_EXIST(10900005,"请求信息不存在"),
    ;

    private Integer code;
    private String reason;

    JdMECodeEnum(Integer code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * 通过code获取具体请求信息
     * @param code
     * @return
     */
    public static String getReasonByCode(Integer code){
        for(JdMECodeEnum reasonCode : JdMECodeEnum.values()){
            if(code.equals(reasonCode.code)){
                return reasonCode.reason;
            }
        }
        return null;
    }

}
