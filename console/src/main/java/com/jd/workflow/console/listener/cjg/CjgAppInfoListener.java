package com.jd.workflow.console.listener.cjg;

import com.jd.jmq.client.consumer.MessageListener;
import com.jd.jmq.common.message.Message;
import com.jd.workflow.console.listener.cjg.processor.CjgAppInfoProcessor;
import com.jd.workflow.soap.common.exception.StdException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
@Slf4j
public class CjgAppInfoListener implements MessageListener {
    @Autowired
    CjgAppInfoProcessor processor;
    @Override
    public void onMessage(List<Message> list) throws Exception {
        log.info("cjg.app_info_message_size={}",list.size());
        processor.dealMessage(list);
        //throw new StdException("消费错误了");
    }
}
