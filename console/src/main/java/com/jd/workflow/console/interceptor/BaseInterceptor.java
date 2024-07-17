package com.jd.workflow.console.interceptor;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.soap.common.util.JsonUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class BaseInterceptor {

    protected void setHttpJsonBody(CommonResult data, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        OutputStream writer = response.getOutputStream();
        String str = JsonUtils.toJSONString(data);
        writer.write(str.getBytes("utf-8"));
        writer.flush();
        writer.close();
        response.flushBuffer();
    }

    protected String getUsername() {
        if (UserSessionLocal.getUser()==null) {
            return null;
        }
        return UserSessionLocal.getUser().getUserId();
    }


    protected static String read(Reader reader) throws IOException {
        StringWriter writer = new StringWriter();
        try {
            write(reader, writer);
            return writer.getBuffer().toString();
        } finally {
            writer.close();
        }
    }

    protected static final int BUFFER_SIZE = 1024 * 8;

    protected static long write(Reader reader, Writer writer) throws IOException {
        int read;
        long total = 0;
        char[] buf = new char[BUFFER_SIZE];
        while ((read = reader.read(buf)) != -1) {
            writer.write(buf, 0, read);
            total += read;
        }
        return total;
    }
}
