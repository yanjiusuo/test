package com.jd.workflow.console.utils;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/20
 */

import com.jd.common.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.jd.workflow.soap.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/20
 */
@Slf4j
public class TestSsoCookieHelper {

    private static final String LOGIN_BODY_FORMAT = "username=%s&password=%s&uuid=%s&loginType=1&fidoInfo=&hioXAesKey=&hioResponseBody=";
    private static final String TEST_SSO_URL = "https://test-ssa.jd.com/sso/login";
    private static final String UTF_8 = "utf-8";
    private static final String COOKIE_PATTER = "Path=/([a-z0-9A-Z\\.=]+?;) Domain=";
    private static Pattern pattern = Pattern.compile(COOKIE_PATTER);

    public static void main(String[] args) {

        String url = TEST_SSO_URL;
        String encoding = UTF_8;
        String user = "wangjingfang3";
        String pwd = "xinxibu456";
        String html = getHTMLResourceByUrl(url, encoding);
        System.out.println(html);
        String body = getSsoLoginBody(html, encoding, user, pwd);

        System.out.println("body:" + body);

        String cookie = getSsoCookie(url, body);
        System.out.println("cookie:" + cookie);
    }

    public static String getUserCookie(String userName, String pwd) {
        String html = getHTMLResourceByUrl(TEST_SSO_URL, UTF_8);
        String body = getSsoLoginBody(html, UTF_8, userName, pwd);
        String cookie = getSsoCookie(TEST_SSO_URL, body);
        if (StringUtils.isEmpty(cookie)) {
            throw new BizException("用户" + userName + "密码错误");
        }
        return cookie;

    }

    private static String getSsoLoginBody(String html, String encoding, String user, String pwd) {
        Document parse = null;

        //解析html，按照什么编码进行解析html
        parse = Jsoup.parse(html, encoding);
        Element pwdInput = parse.getElementById("password");
        String ssoPublicKey = pwdInput.attr("ssoPublicKey");
        Element uuidInput = parse.getElementById("uuid");
        String uuid = uuidInput.attr("value");
        log.info("uuid:{},publickKey:{}", uuid, ssoPublicKey);
        String ssoPwd = RSACoderUtil.encrypt(uuid + pwd, ssoPublicKey);
        String body = String.format(LOGIN_BODY_FORMAT, user, URLEncoder.encode(ssoPwd), uuid);
        return body;

    }

    //获取html
    private static String getHTMLResourceByUrl(String url, String encoding) {
        StringBuffer sb = new StringBuffer();
        URL urlObj = null;
        URLConnection openConnection = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            urlObj = new URL(url);
            openConnection = urlObj.openConnection();
            isr = new InputStreamReader(openConnection.getInputStream(), encoding);
            //建立文件缓冲流
            br = new BufferedReader(isr);
            //建立临时文件
            String temp = null;
            while ((temp = br.readLine()) != null) {
                sb.append(temp + "n");
            }
        } catch (MalformedURLException e) {

            log.error("error message", e);
        } catch (IOException e) {

            log.error("error message", e);
        } finally {
            try {
                if (isr != null) {
                    isr.close();
                }
            } catch (IOException e) {

                log.error("error message", e);
            }
        }
        return sb.toString();
    }

    private static String getSsoCookie(String callUrl, String postBody) {


        String ableUrl = callUrl;


        ResponseEntity responseEntity = null;
        String result = "";
        String line = "";
        BufferedReader reader = null;
        String urlPath = ableUrl;
        HttpURLConnection conn = null;
        int responseCode = 0;
        try {
            URL url = new URL(urlPath);
            if (url.openConnection() instanceof HttpURLConnection) {
                conn = (HttpURLConnection) url.openConnection();
            }
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);                                 // 以后就可以使用conn.getOutputStream().write()
            conn.setDoInput(true);                                  // 以后就可以使用conn.getInputStream().read();
            conn.setUseCaches(false);                               // Post 请求不能使用缓存
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");

            conn.setRequestProperty("accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            conn.setConnectTimeout(60 * 1000 * 10);
            conn.setReadTimeout(60 * 1000 * 10);


            byte[] writebytes = postBody.getBytes();
            conn.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
            OutputStream outwritestream = conn.getOutputStream();
            outwritestream.write(postBody.getBytes());
            outwritestream.flush();
            outwritestream.close();

            responseCode = conn.getResponseCode();
            if (responseCode == 200) {


            } else if (responseCode == 302) {
                //获取cookie
                Map<String, List<String>> map = conn.getHeaderFields();
                if (map.containsKey("Set-Cookie")) {
                    List<String> list = map.get("Set-Cookie");
                    StringBuilder builder = new StringBuilder();
                    for (String str : list) {
                        builder.append(str).toString();
                    }
                    result = builder.toString();
                    System.out.println("得到的cookie=" + result);
                }
                result = matchCookie(result);

            }

        } catch (Exception e) {

            responseCode = 500;
            result = e.getMessage();

        } finally {


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


        return result;
    }

    private static String matchCookie(String input) {
        Matcher matcher = pattern.matcher(input);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String cookie = matcher.group(1);
            result.append(cookie);
        }
        return result.toString();
    }
}
