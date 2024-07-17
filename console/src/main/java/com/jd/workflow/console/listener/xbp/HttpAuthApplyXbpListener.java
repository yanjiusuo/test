package com.jd.workflow.console.listener.xbp;

import com.jd.flow.xbp.listener.BaseXbpJMQ4MessageListener;
import com.jd.workflow.console.entity.HttpAuthApplyXbpParam;
import com.jd.workflow.console.service.IHttpAuthApplyService;
import com.jd.xbp.jmq4.data.BusinessIdBO;
import com.jd.xbp.jmq4.data.Mq4TicketBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO
 *
 * @author xiaobei
 * @date 2022-07-13 20:37
 */
@Slf4j
@Component
public class HttpAuthApplyXbpListener extends BaseXbpJMQ4MessageListener<HttpAuthApplyXbpParam> {

    @Autowired
    private IHttpAuthApplyService httpAuthApplyService;

    @Override
    protected void processTicketClose(BusinessIdBO businessIdBO, Mq4TicketBO ticketBO, HttpAuthApplyXbpParam ticketParam) {
        try {

            httpAuthApplyService.callBackXbpFlow(ticketParam);

        }catch (Exception e) {
            log.error("xbp结单保存Http接口鉴权申请失败", e);
            throw e;
        }
    }

}
