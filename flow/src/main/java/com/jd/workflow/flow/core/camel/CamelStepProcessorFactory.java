package com.jd.workflow.flow.core.camel;


import com.jd.workflow.flow.core.exception.StepScriptEvalException;

import org.apache.camel.*;
import org.apache.camel.model.BeanDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.WhenDefinition;
import org.apache.camel.processor.FilterProcessor;
import org.apache.camel.reifier.ProcessorReifier;
import org.apache.camel.spi.ProcessorFactory;
import org.apache.camel.support.TypedProcessorFactory;
import org.mvel2.CompileException;

import java.util.logging.Filter;

public class CamelStepProcessorFactory extends TypedProcessorFactory<ProcessorDefinition> {

    public CamelStepProcessorFactory() {
        super(ProcessorDefinition.class);
    }

    @Override
    public Processor doCreateProcessor(Route route, ProcessorDefinition definition) throws Exception {
        if(definition instanceof BeanDefinition && CamelStepBean.class.getName().equals(((BeanDefinition)definition).getBeanType())){

            CamelStepBean stepBean = new CamelStepBean();
            stepBean.setCamelContext(route.getCamelContext());
            stepBean.init((BeanDefinition)definition);
            return stepBean;
        }else if(definition instanceof WhenDefinition){
            FilterProcessor processor = (FilterProcessor) ProcessorReifier.reifier(route, definition).createProcessor();
            WrappedFilterProcessor wrappedFilterProcessor = new WrappedFilterProcessor(route.getCamelContext(),processor.getPredicate(),processor.getProcessor());
            wrappedFilterProcessor.definition = definition;
            return wrappedFilterProcessor;
        }
        return null;

    }
    public static final class WrappedFilterProcessor extends FilterProcessor {

        ProcessorDefinition definition;



        public WrappedFilterProcessor(CamelContext context, Predicate predicate, Processor processor) {
            super(context, predicate, processor);
        }

        @Override
        public boolean matches(Exchange exchange) {
            try{
                return super.matches(exchange);
            }catch (Exception e){
                StepScriptEvalException exception = wrapException(e);
                throw exception;
            }
        }



        private StepScriptEvalException wrapException(Exception e){
            StepScriptEvalException exception = new StepScriptEvalException(definition.getParent().getId(),e);
            if(e instanceof ExpressionEvaluationException){
                Throwable cause = e.getCause();
                if(cause instanceof CompileException){
                    CompileException compileException = (CompileException) cause;
                    exception.setLine(compileException.getLineNumber());
                    exception.setDesc(cause.getMessage());
                }
            }

            exception.setStage(definition.getId());
            return exception;
        }
    }
}
