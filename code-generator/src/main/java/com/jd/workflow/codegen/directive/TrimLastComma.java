package com.jd.workflow.codegen.directive;

import freemarker.core.Environment;
import freemarker.template.*;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class TrimLastComma implements TemplateDirectiveModel {
    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        // Check if no parameters were given:
        if (!params.isEmpty()) {
            throw new TemplateModelException(
                    "This directive doesn't allow parameters.");
        }
        if (loopVars.length != 0) {
            throw new TemplateModelException(
                    "This directive doesn't allow loop variables.");
        }

        // If there is non-empty nested content:
        if (body != null) {
            // Executes the nested body. Same as <#nested> in FTL, except
            // that we use our own writer instead of the current output writer.
            body.render(new TrimLastCommaWriter(env.getOut()));
        } else {
            throw new RuntimeException("missing body");
        }
    }

    /**
     * A {@link Writer} that transforms the character stream to upper case
     * and forwards it to another {@link Writer}.
     */
    private static class TrimLastCommaWriter extends Writer {

        private final Writer out;

        TrimLastCommaWriter(Writer out) {
            this.out = out;
        }

        public void write(char[] cbuf, int off, int len)
                throws IOException {
            char[] transformedCbuf = new char[len];
            String s = new String(cbuf);
            s = s.trim();
            if(StringUtils.isEmpty(s)){
                out.write("");
            }else if(s.endsWith(",")){
                s = s.substring(0,s.length() - 1);
                out.write(s);
            }
        }

        public void flush() throws IOException {
            out.flush();
        }

        public void close() throws IOException {
            out.close();
        }
    }
}
