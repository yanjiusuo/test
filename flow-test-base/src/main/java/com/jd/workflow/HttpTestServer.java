package com.jd.workflow;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StringHelper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class HttpTestServer {

    public static void main(String[] args) throws Exception {
       run(6010);
    }
    public static com.sun.net.httpserver.HttpServer run(int port) throws IOException {
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/test", new MyHandler());
        server.createContext("/json", new JsonHandler());
        server.createContext("/form", new FormHandler());
        server.createContext("/null", new NullHandler());
        server.createContext("/echo", new EchoHandler());
        server.createContext("/error", new ErrorHandler());
        server.createContext("/timeout", new TimeoutHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        return server;
    }

    static class EchoHandler implements HttpHandler {
        String[] ignoreKeys = new String[]{
                "Content-type",
                "Accept",
                "Connection",
                "Content-Length",
                "Host",
                "Content-Length",
                "User-Agent",
                "Accept-Encoding"
        };
        Map<String,String> getReqHeaders(Headers headers){
            Map<String,String> ret = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                boolean isMatch = false;
                for (String ignoreKey : ignoreKeys) {
                    if(entry.getKey().equalsIgnoreCase(ignoreKey)) {
                        isMatch = true;
                    }
                }
                if(isMatch) continue;
                ret.put(entry.getKey().toLowerCase(),entry.getValue().get(0));
            }
            return ret;
        }
        @Override
        public void handle(HttpExchange t) throws IOException {
            Map<String,Object> ret = new HashMap<>();

            InputStream reqBody = t.getRequestBody();
            String req = IOUtils.toString(reqBody, "utf-8");
            try{
                ret.put("body",JsonUtils.parse(req));
            }catch (Exception e){
                ret.put("body",req);
            }
            String subPaths = t.getRequestURI().getPath().substring("/echo".length());
            if(!StringUtils.isBlank(subPaths)){
                ret.put("path",subPaths);
            }
            Map<String, String> headers = getReqHeaders(t.getRequestHeaders());
            ret.put("headers",headers);

            String query = t.getRequestURI().getQuery();
            Map<String, Object> params = StringHelper.parseQuery(query, query);
            ret.put("params",params);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                t.getResponseHeaders().add(entry.getKey(),entry.getValue());
            }

            t.getResponseHeaders().add("content-type","application/json");

            String response = JsonUtils.toJSONString(ret);
            log.info("receive_req:{}",response);
            byte[] bytes = response.getBytes();
            t.sendResponseHeaders(200, bytes.length);
            try {
                OutputStream os = t.getResponseBody();
                os.write(bytes);
                os.flush();
                os.close();
            }catch (Exception e){
                log.error("os.err_write_response",e);
            }


        }
    }
    static class JsonHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            InputStream reqBody = t.getRequestBody();
            String response = IOUtils.toString(reqBody, "utf-8");
            t.getResponseHeaders().add("content-type","application/json");
            List<String> token = t.getRequestHeaders().get("token");
            if(token != null && !token.isEmpty()){
                t.getResponseHeaders().add("token",token.get(0));
            }

            byte[] bytes = response.getBytes();
            t.sendResponseHeaders(200, bytes.length);
            try {
                OutputStream os = t.getResponseBody();
                os.write(bytes);
                os.flush();
                os.close();
            }catch (Exception e){
                log.error("os.err_write_response",e);
            }


        }
    }
    static class ErrorHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            t.sendResponseHeaders(400, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    static class TimeoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            URI uri = t.getRequestURI();
            Map<String, Object> params = StringHelper.parseQuery(uri.getQuery(), "utf-8");
            Integer timeout = Variant.valueOf(params.get("timeout")).toInt();
            if(timeout != null){
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    static class NullHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.sendResponseHeaders(201, 0);
            OutputStream os = t.getResponseBody();
            os.write("".getBytes());
            os.close();
        }
    }

    static class FormHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            InputStream reqBody = t.getRequestBody();
            String body = IOUtils.toString(reqBody, "utf-8");

            Map<String, Object> map = StringHelper.parseQuery(body, "utf-8");

            String response = JsonUtils.toJSONString(map);
            byte[] bytes = response.getBytes("utf-8");
            t.getResponseHeaders().add("content-type","application/json");
            t.sendResponseHeaders(200, bytes.length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }


    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

}
