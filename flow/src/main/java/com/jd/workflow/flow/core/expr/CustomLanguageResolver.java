package com.jd.workflow.flow.core.expr;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.engine.DefaultLanguageResolver;
import org.apache.camel.spi.Language;

public class CustomLanguageResolver extends DefaultLanguageResolver {
    @Override
    public Language resolveLanguage(String name, CamelContext context) {
        if("mvel".equals(name)){
            return new CustomMvelLanguage();
        }
        return super.resolveLanguage(name, context);
    }
}
