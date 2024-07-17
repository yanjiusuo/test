package com.jd.workflow.flow.bean.ducc;


import com.jd.workflow.flow.core.bean.IBeanFactory;
import com.jd.workflow.flow.core.bean.IBeanStepProcessor;

import java.util.Collections;
import java.util.List;

public class DuccBeanProcessor implements IBeanStepProcessor<DuccFlowBean,DuccConfig> {
    @Override
    public String getName() {
        return "ducc";
    }

    @Override
    public IBeanFactory getBeanFactory() {
        return new DuccBeanInitFactory();
    }

    @Override
    public Class<DuccConfig> getInitConfigClass() {
        return DuccConfig.class;
    }



    @Override
    public Class getBeanType() {
        return DuccFlowBean.class;
    }
}
