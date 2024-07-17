package com.jd.workflow.flow.core.output;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.jd.workflow.flow.core.definition.StepDefinition;
import com.jd.workflow.flow.core.exception.ErrorMessageFormatter;
import com.jd.workflow.flow.core.exception.StepExecException;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.soap.common.exception.StdException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionSerializer  extends JsonSerializer<Throwable> {

    @Override
    public void serialize(Throwable value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String exeptionMsg = null;
        if(value instanceof StepExecException){
            exeptionMsg = ErrorMessageFormatter.formatMsg((StepExecException)value);
        }else if(value instanceof StepParseException){
            exeptionMsg = ErrorMessageFormatter.formatMsg((StepParseException)value);
        }else if(value instanceof StdException){
            exeptionMsg = ErrorMessageFormatter.formatMsg((StdException)value);
        }else{
            StringWriter stringWriter = new StringWriter();
            value.printStackTrace(new PrintWriter(stringWriter));
            exeptionMsg = stringWriter.toString();
        }

        gen.writeString(exeptionMsg);

    }
}
