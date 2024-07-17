package com.jd.workflow;

import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.jme.JdMEMessageUtil;
import com.jd.workflow.console.jme.JdMENoticeMessage;
import com.jd.workflow.console.jme.JdMEResult;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author wufagang
 * @description
 * @date 2023年05月10日 15:59
 */
public class JMETest {

    private String NOTICETEMPLATE = "（%s）接口信息已发生变化，请于接口详情页查看接口变更记录。";
    private String NOTICEURL = "http://console.paas.jd.com/idt/online/interface/interfaceDetail/%d/%d";
    @Test
    public void sendMessageTest() {
        InterfaceManage manage = new InterfaceManage();
        manage.setName("测试");
        manage.setType(1);
        manage.setId(12502l);
        Set<String> interfaceFollowUser = new HashSet<>();
        interfaceFollowUser.add("wufagang");
        if(org.apache.commons.collections4.CollectionUtils.isEmpty(interfaceFollowUser)) return;
        JdMENoticeMessage noticeMessage = new JdMENoticeMessage();
        noticeMessage.setTitle("接口文档变更通知");
        noticeMessage.setContent(String.format(NOTICETEMPLATE,manage.getName()));
        noticeMessage.setTos(interfaceFollowUser.toArray(new String[interfaceFollowUser.size()]));
        JdMENoticeMessage.Extend extend = new JdMENoticeMessage.Extend();
        extend.setUrl(String.format(NOTICEURL,manage.getType(),manage.getId()));
        noticeMessage.setExtend(extend);
        JdMEResult<String> jdMeResult = JdMEMessageUtil.sendMessage(noticeMessage);
        System.out.println("京东ME消息发送结果为："+ JSON.toJSONString(jdMeResult));
    }

}
