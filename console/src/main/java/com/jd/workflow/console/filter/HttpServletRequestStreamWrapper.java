package com.jd.workflow.console.filter;

import lombok.*;
import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 项目名称：example
 * 类 名 称：HttpServletRequestStreamWrapper
 * 类 描 述：RequestStream包装
 * 创建时间：2022-05-25 09:29
 * 创 建 人：wangxiaofei8
 */
@Getter
@Setter
public class HttpServletRequestStreamWrapper extends HttpServletRequestWrapper {

    private byte[] requestBody = null;
    HttpServletRequest request;
    public HttpServletRequestStreamWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.request = request;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if(requestBody == null){
            requestBody = StreamUtils.copyToByteArray(request.getInputStream());
        }

         ByteArrayInputStream bais = new ByteArrayInputStream(requestBody);

        return new ServletInputStream() {

            @Override
            public int read() throws IOException {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException{
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }
}

