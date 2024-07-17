package com.jd.workflow.flow.bean.jimdb;

import com.jd.jim.cli.ReloadableJimClientFactory;
import com.jd.workflow.flow.bean.ducc.DuccConfig;
import com.jd.workflow.flow.bean.ducc.DuccFlowBean;
import com.jd.workflow.flow.core.bean.IBeanFactory;

public class JimdbBeanInitFactory implements IBeanFactory<JimdbFlowBean, JimdbConfig> {



    @Override
    public JimdbFlowBean init(JimdbConfig config) {
        JimdbFlowBean bean = new JimdbFlowBean(config);
        bean.init();
        return bean;
    }


    @Override
    public void destroy(JimdbFlowBean bean) {
        bean.destroy();
    }
}
