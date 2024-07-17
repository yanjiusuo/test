package com.jd.workflow.server.service;

import com.jd.workflow.flow.core.exception.ErrorMessageFormatter;
import com.jd.workflow.soap.common.exception.StdException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Service
public class InitService implements InitializingBean {
    /**
     * 线程池里ErrorMessageFormatter里不能初始化，现在在spring初始化的时候初始化错误处理器
      * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        ErrorMessageFormatter.formatMsg(new StdException("xxx"));
    }
}
