package com.jd.workflow.console.utils;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/11/2
 */

import com.alibaba.fastjson.JSONObject;
import com.jd.jim.cli.Cluster;
import io.netty.handler.timeout.TimeoutException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/11/2 
 */
@Data
@Slf4j
public class RestTemplateUtils {
    /**
     *
     */
    private RestTemplate restTemplate;
    /**
     *
     */
    private HttpHeaders headers;

    /**
     * 发送 json 结构的post请求
     *
     * @param url        请求url地址
     * @param jsonParams json 入参
     * @param retClass   return 类型
     */
    public <T> T postJson(String url, String jsonParams, Class<T> retClass) {
        String uuid = UUID.randomUUID().toString();
        log.info("uuid {} url {},param {}", uuid, url, jsonParams);

        String hostName = this.getHostFromUrl(url);
        String hostIp = this.getHostIp(hostName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.isNotEmpty(hostIp)) {
//            System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
            url = url.replace(hostName, hostIp);
            log.info("新url:{}。hostName:{}. System.getProperty:{}", url, hostName, System.getProperty("sun.net.http.allowRestrictedHeaders"));
            headers.add(HttpHeaders.HOST, hostName);
        }

        // 缓存中没有IP，
        HttpEntity<String> request = new HttpEntity<>(jsonParams, headers);
        T result = restTemplate.postForObject(url, request, retClass);
        log.info("uuid {}  url {},response: {}", uuid, url, result);
        return result;
    }

    public <T> T postJson(String url, String jsonParams, Class<T> retClass, HttpHeaders headers) {
        String uuid = UUID.randomUUID().toString();
        log.info("uuid {} url {},param {}", uuid, url, jsonParams);

        String hostName = this.getHostFromUrl(url);
        String hostIp = this.getHostIp(hostName);
        if (StringUtils.isNotEmpty(hostIp)) {
            url = url.replace(hostName, hostIp);
            log.info("新url:{}。hostName:{}. System.getProperty:{}", url, hostName, System.getProperty("sun.net.http.allowRestrictedHeaders"));
            headers.add(HttpHeaders.HOST, hostName);
        }
        HttpEntity<String> request = new HttpEntity<>(jsonParams, headers);
        T result = restTemplate.postForObject(url, request, retClass);
        log.info("uuid {}, url {},response: {}", uuid, url, result);
        return result;
    }

    public String postJson(String url, String jsonParams) {
        String uuid = UUID.randomUUID().toString();
        log.info("uuid {} url {},param {}", uuid, url, jsonParams);

        String hostName = this.getHostFromUrl(url);
        String hostIp = this.getHostIp(hostName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.isNotEmpty(hostIp)) {
            url = url.replace(hostName, hostIp);
            log.info("新url:{}。hostName:{}. System.getProperty:{}", url, hostName, System.getProperty("sun.net.http.allowRestrictedHeaders"));
            headers.add(HttpHeaders.HOST, hostName);
        }

        HttpEntity<String> request = new HttpEntity<>(jsonParams, headers);
        String retStr = restTemplate.postForObject(url, request, String.class);
        log.info("uuid {} url {},response: {}", uuid, url, retStr);
        return retStr;

    }

    public String sendGet(String url) {
        String uuid = UUID.randomUUID().toString();
        log.info("uuid {}  geturl {}", uuid, url);

        String hostName = this.getHostFromUrl(url);
        String hostIp = this.getHostIp(hostName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.isNotEmpty(hostIp)) {
            url = url.replace(hostName, hostIp);
            log.info("新url:{}。hostName:{}. System.getProperty:{}", url, hostName, System.getProperty("sun.net.http.allowRestrictedHeaders"));
            headers.add(HttpHeaders.HOST, hostName);
        }
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        } catch (TimeoutException e) {
            log.error("TimeoutException");
        }
        log.info("uuid {}  geturl {} response : {} ", uuid, url, responseEntity.getBody());
        return responseEntity.getBody();

    }

    public String sendGetOnly(String url) {
        String uuid = UUID.randomUUID().toString();
        log.info("sendGetOnly uuid {}  get-url {}", uuid, url);
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = new MediaType("application", "json", StandardCharsets.UTF_8);
        headers.setContentType(mediaType);
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = null;
        responseEntity = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        log.info("sendGetOnly uuid {}  get-url {} response : {} ", uuid, url, responseEntity.getBody());
        return responseEntity.getBody();

    }

    /**
     *
     */
    public ResponseEntity post(String url, HttpHeaders headers, MultiValueMap param) {

        String hostName = this.getHostFromUrl(url);
        String hostIp = this.getHostIp(hostName);
        if (StringUtils.isNotEmpty(hostIp)) {
            url = url.replace(hostName, hostIp);
            log.info("新url:{}。hostName:{}. System.getProperty:{}", url, hostName, System.getProperty("sun.net.http.allowRestrictedHeaders"));
            headers.add(HttpHeaders.HOST, hostName);
        }

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(param, headers);
        return restTemplate.postForEntity(url, request, String.class);
    }

    /**
     *
     */
    public String post(String url, HttpHeaders headers, String body) {

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        return restTemplate.postForObject(url, request, String.class);
    }



    /**
     *
     */
    public String get(String url, HttpHeaders headers) {
        String hostName = this.getHostFromUrl(url);
        String hostIp = this.getHostIp(hostName);
        if (StringUtils.isNotEmpty(hostIp)) {
            url = url.replace(hostName, hostIp);
            log.info("新url:{}。hostName:{}. System.getProperty:{}", url, hostName, System.getProperty("sun.net.http.allowRestrictedHeaders"));
            headers.add(HttpHeaders.HOST, hostName);
        }

        HttpEntity<String> request = new HttpEntity<>("", headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        return response.getBody();
    }



    public ResponseEntity<String> getByHttpUrlConnection(String urlPath, Map<String, String> headerMap) {
        ResponseEntity<String> responseEntity;
        StringBuilder result = new StringBuilder();
        String line;
        BufferedReader reader = null;

        HttpURLConnection conn = null;
        int responseCode = 0;
        try {
            conn = this.getProxyConnection(urlPath);

            conn.setRequestMethod("GET");
            conn.setDoOutput(true);                                 // 以后就可以使用conn.getOutputStream().write()
            conn.setDoInput(true);                                  // 以后就可以使用conn.getInputStream().read();
            conn.setUseCaches(false);                               // Post 请求不能使用缓存
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("accept", "application/json");
            conn.setConnectTimeout(6 * 1000);
            conn.setReadTimeout(6 * 1000);
            if (!CollectionUtils.isEmpty(headerMap)) {
                for (Map.Entry<String, String> row : headerMap.entrySet()) {
                    conn.setRequestProperty(row.getKey(), row.getValue());
                }
            }

            responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } else {
                if (conn.getErrorStream() != null) {
                    reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                } else {
                    result = new StringBuilder("errorStream 为 null");
                }
            }

        } catch (Exception e) {
            responseCode = 500;
            result = new StringBuilder(e.getMessage());
            log.error("getByHttpURLConnection执行出错。urlPath:{}", urlPath, e);
        } finally {
            responseEntity = new ResponseEntity<>(result.toString(), HttpStatus.resolve(responseCode));
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("关闭http资源出错", e);
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return responseEntity;
    }

    public ResponseEntity<String> postByHttpUrl(String ableUrl, Map postBodyMap, Map<String, String> headerMap) {
        ResponseEntity<String> responseEntity;
        StringBuilder result = new StringBuilder();
        String line;
        BufferedReader reader = null;
        HttpURLConnection conn = null;
        int responseCode = 0;
        try {
//            URL url = new URL(ableUrl);
//            conn = (HttpURLConnection) url.openConnection();
            conn = this.getProxyConnection(ableUrl);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);                                 // 以后就可以使用conn.getOutputStream().write()
            conn.setDoInput(true);                                  // 以后就可以使用conn.getInputStream().read();
            conn.setUseCaches(false);                               // Post 请求不能使用缓存
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("accept", "application/json");
            conn.setConnectTimeout(60 * 1000 * 1);
            conn.setReadTimeout(60 * 1000 * 1);

            if (!CollectionUtils.isEmpty(headerMap)) {
                for (Map.Entry<String, String> row : headerMap.entrySet()) {
                    conn.setRequestProperty(row.getKey(), row.getValue());
                }
            }

            if (postBodyMap != null) {
                String json = JSONObject.toJSONString(postBodyMap);
                byte[] writebytes = json.getBytes();
                conn.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
                OutputStream outwritestream = conn.getOutputStream();
                outwritestream.write(json.getBytes());
                outwritestream.flush();
                outwritestream.close();
            }

            responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            }
            else if(responseCode == 201){
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            }
            else {

                if (conn.getErrorStream() != null) {
                    reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                } else {
                    result = new StringBuilder("errorStream 为 null");
                }
            }
        } catch (Exception e) {
            log.error("post->   POST 请求失败, ", e);
            responseCode = 500;
            result = new StringBuilder(e.getMessage());
        } finally {
            responseEntity = new ResponseEntity<>(result.toString(), HttpStatus.resolve(responseCode));
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }

        log.info("post->  rpc POST 请求返回状态: " + responseEntity.getStatusCode() + " 内容: " + responseEntity.getBody());

        return responseEntity;
    }

    /**
     * 转换文件大小
     */
    public static String formatFileSize(Long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == null || fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 设置host
     *
     * @param urlStr 请求url
     * @return conn
     */
    public HttpURLConnection getProxyConnection(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        String hostName = url.getHost();
        String hostIp = this.getHostIp(hostName);   //从缓存中根据域名获取对应的IP
        HttpURLConnection httpUrlConnection=null;

        boolean Safe = jdSsrfCheck(url);
        if (!Safe) {
            log.info("url[{}]不在白名单里，不进行代理", urlStr);
//            return null;
        }
        if (StringUtils.isEmpty(hostIp)) {
            log.warn("未取到hostName[{}]的ip.", hostName);
            if(url.openConnection() instanceof  HttpURLConnection) {
//                boolean Safe = SafeUtils.jdSsrfCheck(url);
//                if(Safe){
                httpUrlConnection = (HttpURLConnection) url.openConnection();
//                }else{
                log.warn("hostName[{}]安全认证未通过.", hostName);
                return httpUrlConnection;
//                }
            }
            return httpUrlConnection;
        }
        byte[] ip = new byte[4];
        try {
            String[] arr = hostIp.split("\\.");
            for (int i = 0; i < 4; i++) {
                int tmp = Integer.parseInt(arr[i]);
                ip[i] = (byte) tmp;
            }
            //生成proxy代理对象，因为http底层是socket实现
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(InetAddress.getByAddress(ip), 80));
            if(url.openConnection() instanceof  HttpURLConnection) {
                httpUrlConnection = (HttpURLConnection) url.openConnection(proxy);
            }

        } catch (Exception e) {
            log.error("设置HOST出错。urlStr:{}, hostIp:{}", urlStr, hostIp, e);
            if(url.openConnection() instanceof  HttpURLConnection) {
                httpUrlConnection = (HttpURLConnection) url.openConnection();
            }

        }
        return httpUrlConnection;
    }

    private String getHostIpByHostName(String requestUrl) throws Exception {
        String hostName = this.getHostFromUrl(requestUrl);
        return this.getHostIp(hostName);
    }

    /**
     * 解析出请求url中的HostName
     *
     * @param requestUrl 请求url
     * @return hostName
     */
    private String getHostFromUrl(String requestUrl) {
        try {
            if (!requestUrl.startsWith("http://") && !requestUrl.startsWith("https://")) {
                requestUrl = "http://" + requestUrl;
            }
            URL url = new URL(requestUrl);
            return url.getHost();
        } catch (Exception e) {
            log.error("从URL中提取host失败.requestUrl:{}", requestUrl, e);
            return requestUrl;
        }
    }


    public boolean jdSsrfCheck(URL urlObj){
        //定义请求协议白名单列表
        String[] allowProtocols = new String[]{"http", "https"};
        //定义请求域名白名单列表，根据业务需求进行配置
        String[] allowDomains = new String[]{"www.jd.com,bamboo.jd.com,git.jd.com,coding.jd.com,test.jdos.jd.com"};
        //定义请求端口白名单列表
        int[] allowPorts = new int[]{80, 443};
        boolean ssrfCheck = false, protocolCheck = false, domianCheck = false;

        // 首先进行协议校验，若协议校验不通过，SSRF校验不通过
        String protocol = urlObj.getProtocol();
        for(String item : allowProtocols){
            if(protocol.equals(item)){
                protocolCheck = true;
                break;
            }
        }
        // 协议校验通过后，再进行域名校验，反之不进行域名校验，SSRF校验不通过
        if(protocolCheck){
            String host = urlObj.getHost();
            for(String domain: allowDomains){
                if(domain.equals(host)){
                    domianCheck = true;
                    break;
                }
            }
        }
        //域名校验通过后，再进行端口校验，反之不进行端口校验，SSRF校验不通过
        if(domianCheck){
            int port = urlObj.getPort();
            if(port == -1) {
                port = 80;
            }
            for (Integer item : allowPorts) {
                if (item == port) {
                    ssrfCheck = true;
                    break;
                }
            }
        }
        if(ssrfCheck){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 根据hostName从缓存中指定域名的ip
     */
    private String getHostIp(String hostName) {
        return "";
    }


    public String postJsonWithRetry(String url, String jsonParams, int retryCount) {
        String uuid = UUID.randomUUID().toString();
        log.info("uuid {} url {},param {}", uuid, url, jsonParams);
        String hostName = this.getHostFromUrl(url);
        String hostIp = this.getHostIp(hostName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.isNotEmpty(hostIp)) {
            url = url.replace(hostName, hostIp);
            log.info("新url:{}。hostName:{}. System.getProperty:{}", url, hostName, System.getProperty("sun.net.http.allowRestrictedHeaders"));
            headers.add(HttpHeaders.HOST, hostName);
        }
        HttpEntity<String> request = new HttpEntity<>(jsonParams, headers);
        String retStr = null;
        try {
            retStr = restTemplate.postForObject(url, request, String.class);
        } catch (Exception e) {
            for (int i = 1; i <= retryCount; i++) {
                if (i < retryCount) {
                    try {
                        retStr = restTemplate.postForObject(url, request, String.class);
                        if (retStr != null) {
                            break;
                        }
                    } catch (Exception e1) {
                        log.info("重试发送第{}次,url:{},错误信息{}", i, url, e1.getMessage());
                    }
                } else {
                    retStr = restTemplate.postForObject(url, request, String.class);
                }
            }
        }
        log.info("uuid {} url {},response: {}", uuid, url, retStr);
        return retStr;
    }

    /**
     * 自定义headers
     * @param url
     * @param jsonParams
     * @param headers
     * @return
     */
    public String postJson(String url, String jsonParams, HttpHeaders headers ) {
        String uuid = UUID.randomUUID().toString();
        log.info("uuid {} url {},param {}", uuid, url, jsonParams);

        String hostName = this.getHostFromUrl(url);
        String hostIp = this.getHostIp(hostName);
        if (StringUtils.isNotEmpty(hostIp)) {
            url = url.replace(hostName, hostIp);
            log.info("新url:{}。hostName:{}. System.getProperty:{}", url, hostName, System.getProperty("sun.net.http.allowRestrictedHeaders"));
            headers.add(HttpHeaders.HOST, hostName);
        }

        HttpEntity<String> request = new HttpEntity<>(jsonParams, headers);
        String retStr = restTemplate.postForObject(url, request, String.class);
        log.info("uuid {} url {},response: {}", uuid, url, retStr);
        return retStr;
    }

    /**
     * 接口返回值中文乱码-切换为此接口
     * 接收编码指定为UTF-8
     */
    public String getAccept(String url, HttpHeaders headers) {
        String hostName = this.getHostFromUrl(url);
        String hostIp = this.getHostIp(hostName);
        if (StringUtils.isNotEmpty(hostIp)) {
            url = url.replace(hostName, hostIp);
            log.info("新url:{}。hostName:{}. System.getProperty:{}", url, hostName, System.getProperty("sun.net.http.allowRestrictedHeaders"));
            headers.add(HttpHeaders.HOST, hostName);
        }

        HttpEntity<String> request = new HttpEntity<>("", headers);
        List<HttpMessageConverter<?>> httpMessageConverters = restTemplate.getMessageConverters();
        httpMessageConverters.stream().forEach(httpMessageConverter -> {
            if(httpMessageConverter instanceof StringHttpMessageConverter){
                StringHttpMessageConverter messageConverter = (StringHttpMessageConverter) httpMessageConverter;
                messageConverter.setDefaultCharset(Charset.forName("UTF-8"));
            }
        });
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        return response.getBody();
    }


}
