package com.jd.workflow.flow.bean.ducc;

import com.jd.laf.config.Configuration;
import com.jd.laf.config.ConfiguratorManager;
import com.jd.laf.config.Property;
import com.jd.laf.config.Resource;
import com.jd.workflow.flow.core.bean.IBeanFactory;

import java.util.List;

public class DuccBeanInitFactory implements IBeanFactory<DuccFlowBean,DuccConfig> {



    @Override
    public DuccFlowBean init(DuccConfig config) {
        DuccFlowBean bean = new DuccFlowBean(config);
        bean.init();
        return bean;
    }

    @Override
    public void destroy(DuccFlowBean bean) {
        bean.destroy();
    }
}
