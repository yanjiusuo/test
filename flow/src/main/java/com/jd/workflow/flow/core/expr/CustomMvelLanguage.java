package com.jd.workflow.flow.core.expr;

import org.apache.camel.Expression;
import org.apache.camel.ExpressionIllegalSyntaxException;
import org.apache.camel.Predicate;
import org.apache.camel.impl.engine.DefaultLanguageResolver;
import org.apache.camel.language.mvel.MvelExpression;
import org.apache.camel.language.mvel.MvelLanguage;
import org.apache.camel.spi.Language;
import org.apache.camel.spi.ScriptingLanguage;
import org.apache.camel.support.LanguageSupport;
import org.mvel2.integration.PropertyHandlerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomMvelLanguage extends LanguageSupport implements ScriptingLanguage {
    static{
        //PropertyHandlerFactory.registerPropertyHandler(Map.class,new MapPropertyHandler());
        //PropertyHandlerFactory.registerPropertyHandler(List.class,new CollectionPropertyHandler());

    }
    @Override
    public Predicate createPredicate(String expression) {
        expression = loadResource(expression);
        return new CustomMvelExpression(this, expression, Boolean.class);
    }

    @Override
    public Expression createExpression(String expression) {
        expression = loadResource(expression);
        return new CustomMvelExpression(this, expression, Object.class);
    }

    @Override
    public <T> T evaluate(String script, Map<String, Object> bindings, Class<T> resultType) {
        script = loadResource(script);
        try {
            Serializable compiled = org.mvel2.MVEL.compileExpression(script);
            Object value = org.mvel2.MVEL.executeExpression(compiled, bindings);
            return getCamelContext().getTypeConverter().convertTo(resultType, value);
        } catch (Exception e) {
            throw new ExpressionIllegalSyntaxException(script, e);
        }
    }
}
