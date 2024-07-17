package com.jd.workflow.flow.bean.jimdb;


import com.jd.workflow.flow.bean.ducc.DuccBeanInitFactory;
import com.jd.workflow.flow.bean.ducc.DuccConfig;
import com.jd.workflow.flow.bean.ducc.DuccFlowBean;
import com.jd.workflow.flow.core.bean.IBeanFactory;
import com.jd.workflow.flow.core.bean.IBeanStepProcessor;

public class JimdbBeanProcessor implements IBeanStepProcessor<JimdbFlowBean, JimdbConfig> {
    @Override
    public String getName() {
        return "jimdb";
    }

    @Override
    public IBeanFactory getBeanFactory() {
        return new JimdbBeanInitFactory();
    }

    @Override
    public Class<JimdbConfig> getInitConfigClass() {
        return JimdbConfig.class;
    }



    @Override
    public Class getBeanType() {
        return JimdbFlowBean.class;
    }
}
