package com.jd.workflow;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StringHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
@Slf4j
public class HttpServer {

    public static void main(String[] args) throws Exception {
        run(6010);
    }
    public static void run(int port) throws IOException {
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/test", new MyHandler());
        server.createContext("/json", new JsonHandler());
        server.createContext("/form", new FormHandler());
        server.createContext("/null", new NullHandler());
        server.createContext("/error", new ErrorHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
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
            t.getResponseHeaders().add("content-type","application/json");
            t.sendResponseHeaders(200, response.length());
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
