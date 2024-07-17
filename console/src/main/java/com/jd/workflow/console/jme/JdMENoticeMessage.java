package com.jd.workflow.console.jme;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 京东ME通知消息体
 * 参见：http://regist-open.timline.jd.com/#/api-list
 * @author xiaobei
 * @date 2022-12-21 19:42
 */
@Getter
@Setter
@ToString
public class JdMENoticeMessage {

    /**
     * 通知消息申请是填的通知标识，带上~符号，例如 ~test001
     *
     * 目前，藏经阁侧申请的通知标识为：~cjgpaas
     *
     * **必填**，最大长度：32
     */
    @NotNull(message = "通知标识 notice_id 不能为空")
    @Size(max = 32, message = "通知标识 notice_id 超时最大长度")
    private String notice_id;

    /**
     * 接收用户的租户appId
     * 国内：ee, 泰国:th.ee, 印尼:id.ee, 赛夫:sf.ee
     *
     * **必填**
     */
    private String app;

    /**
     * 通知标题
     *
     * **必填**，最大长度：100
     */
    @NotNull(message = "通知标题 title 不能为空")
    @Size(max = 100, message = "通知标题 title 超时最大长度")
    private String title;

    /**
     * 消息内容
     *
     * **必填**，最大长度：2000
     */
    @NotNull(message = "消息内容 content 不能为空")
    @Size(max = 2000, message = "消息内容 content 超时最大长度")
    private String content;

    /**
     * 推送终端: 填写对应的数值
     * 全部终端 : 7
     * 单个终端:
     *    apad端 : 32768
     *    window端 :65536
     *    ios端 :131072
     *    android端: 262144
     *    ipad端 : 8388608
     *    mac端 : 16777216
     * 组合终端：
     *    window+mac端：16842752
     *    ios + andrioid端：393216
     *    ios + andrioid+ipad+apad端：8814592
     *    注：其他自定义组合终端按单个终端的数值相加即可
     * 旧的组合终端
     *    桌面端: 1, 包含(pc+mac+ipad+apad)
     *    移动端: 2, 包含 ( ios + andrioid)
     *
     * **必填**
     */
    @NotNull(message = "推送终端 to_terminal 不能为空")
    private Integer to_terminal;

    /**
     * 推送的erp数组，最大数量500
     *
     * **必填**，最大长度：500
     */
    @NotEmpty(message = "推送的erp数组 tos 不能为空")
    @Size(max = 500, message = "推送的erp数组 tos 超时最大长度")
    private String[] tos;

    /**
     * 通知时间
     *
     * 非必填
     */
    private Long notice_time;

    /**
     * 有跳转链接时，跳转链接相关的信息
     */
    private Extend extend;

    /**
     * 京ME参数
     * {@link Infox}
     */
    private Infox infox;

    /**
     *
     */
    @Getter
    @Setter
    @ToString
    public static class Extend {

        /**
         * 内容图片url地址，没有则不填
         */
        private String pic;

        /**
         * 点击通知查看详情可跳转的url，url中含有中文时，请将中文进行urlencode编码，
         * 如果推送了移动端京ME并且需要跳转H5应用，则填写deepLink地址，参考下方deepLink的格式
         */
        private String url;
    }


    /**
     * 京ME参数
     */
    @Getter
    @Setter
    @ToString
    public static class Infox {

        /**
         * 如果推送了移动端京ME,点击查看详情需要打开H5应用地址，则需要使用deepLink地址跳转，格式为：jdme://web/201907180554?url=http://test.jd.com
         * 201907180554：京Me的H5应用编码
         * url：跳转的H5应用页面路径,需要urlencode编码
         */
        private String deepLink;
    }

}


