package com.jd.workflow.console.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.jd.jsf.gd.GenericService;
import com.jd.jsf.gd.codec.msgpack.JSFMsgPack;
import com.jd.jsf.gd.config.ConsumerConfig;
import com.jd.jsf.gd.config.RegistryConfig;
import com.jd.workflow.console.dto.flow.param.JsfOutputExt;
import com.jd.workflow.console.dto.jsf.HttpDebugDto;
import com.jd.workflow.console.dto.jsf.JarJsfDebugDto;
import com.jd.workflow.console.dto.jsf.NewJsfDebugDto;
import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.flow.core.input.HttpInput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.output.UploadDTO;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.utils.ParamMappingContext;
import com.jd.workflow.flow.utils.ParametersUtils;
import com.jd.workflow.jsf.cast.JsfParamConverterRegistry;
import com.jd.workflow.jsf.enums.JsfRegistryEnvEnum;
import com.jd.workflow.jsf.input.JsfOutput;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.*;

@Slf4j
public class RpcUtils {
    static CloseableHttpClient client = HttpClientBuilder.create().build();
    static ParametersUtils utils = new ParametersUtils();

    public static HttpOutput callHttp(HttpInput input, String targetAddress, String host) {
        //HttpEntityEnclosingRequestBase req ;
        HttpRequestBase req;
        if ("post".equalsIgnoreCase(input.getMethod())) {
            req = new HttpPost();
        } else if ("get".equalsIgnoreCase(input.getMethod())) {
            req = new HttpGet();
        } else if ("put".equalsIgnoreCase(input.getMethod())) {
            req = new HttpPut();
        } else if ("options".equalsIgnoreCase(input.getMethod())) {
            req = new HttpOptions();
        } else {
            req = new HttpDelete();
        }
        ContentType contentType = null;
        if (ReqType.form.equals(input.getReqType())) {

            contentType = ContentType.APPLICATION_FORM_URLENCODED.withCharset("utf-8");
            if (input.getBody() != null && input.getBody() instanceof Map) {
                input.setBody(StringHelper.encodeQuery((Map<String, ?>) input.getBody(), "utf-8"));
            }
        } else if (ReqType.json.equals(input.getReqType())) {
            contentType = ContentType.APPLICATION_JSON.withCharset("utf-8");
        } else if (ReqType.xml.equals(input.getReqType())) {
            contentType = ContentType.TEXT_XML.withCharset("utf-8");
        } else {
            if (!StringUtils.isEmpty(input.getContentType())) {
                contentType = ContentType.create(input.getContentType(), "utf-8");
            } else {
                contentType = ContentType.TEXT_PLAIN.withCharset("utf-8");
            }
        }

        if (input.getHeaders() != null) {
            for (Map.Entry<String, Object> entry : input.getHeaders().entrySet()) {
                if (entry.getKey().equalsIgnoreCase("content-type")) continue;
                req.addHeader(entry.getKey(), Variant.valueOf(entry.getValue()).toString());
            }
        }
        req.addHeader("Content-Type", contentType.toString());
        try {
            String queryStr = StringHelper.encodeQuery(input.getParams(), "utf-8");
            String uri = targetAddress;
            if(!uri.startsWith("http://") && !uri.startsWith("https://")){
                throw new BizException("url地址必须包含http://或者https://");
            }
            if (!StringHelper.isEmpty(queryStr)) {
                uri = uri + "?" + queryStr;
            }

            req.setURI(new URI(uri));
            String body = null;
            if (req instanceof HttpEntityEnclosingRequestBase && !ObjectHelper.isEmpty(input.getBody())) {
                if (!(input.getBody() instanceof String)) {
                    body = JsonUtils.toJSONString(input.getBody());
                } else {
                    body = (String) input.getBody();
                }

                ((HttpEntityEnclosingRequestBase) req).setEntity(new StringEntity(body, contentType));
            }


            System.out.println("http.request_uri:url=" + uri + ",method=" + input.getMethod() + ",body=" + body);
            CloseableHttpResponse response = client.execute(req);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpOutput output = new HttpOutput();
            output.setStatus(statusCode);
            output.setSize((int) response.getEntity().getContentLength());
            for (Header header : response.getAllHeaders()) {
                output.getHeaders().put(header.getName(), header.getValue());
            }

            if (response.getEntity() != null) {
                String result = EntityUtils.toString(response.getEntity(), "utf-8");
                System.out.print("http.success_request_uri:url=" + uri + ",method=" + input.getMethod() + ",code=" + statusCode + ",body=" + body + ",response=" + result);
                try {
                    output.setBody(JsonUtils.parse(result));
                } catch (Exception e) {
                    output.setBody(result);
                }

            } else {
                //log.info("http.success_request_uri:url={},method={},code={},body={},response={}",uri,input.getMethod(),statusCode,body,"");
                System.out.print("http.success_request_uri:url=" + uri + ",method=" + input.getMethod() + ",code=" + statusCode + ",body=" + body + ",response=");

            }
            return output;
        } catch (Exception e) {
            HttpOutput output = new HttpOutput();
            output.setSuccess(false);
            if (e instanceof RuntimeException) {
                output.setException((RuntimeException) e);
            } else {
                RuntimeException exception = new RuntimeException(e);
                output.setException(exception);
            }

            return output;
            //throw StdException.adapt(e);
        }
    }

    public static HttpOutput callHttpFile(HttpDebugDto dto, String targetAddress, String host) {
        HttpPost post = new HttpPost(targetAddress);
        UploadDTO uploadDTO = getInputStream(dto);

        HttpOutput output = new HttpOutput();
        output.setSuccess(false);
        if (Objects.isNull(uploadDTO.getInputStream())) {

            return output;
        }
        try {
            if (dto.getInput().getHeaders() != null) {
                Map<String, Object> paramMap = utils.buildInput(dto.getInput().getHeaders());
                for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase("content-type")) continue;
                    post.addHeader(entry.getKey(), Variant.valueOf(entry.getValue()).toString());
                }
            }

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addBinaryBody(uploadDTO.getFieldName(), uploadDTO.getInputStream(), ContentType.MULTIPART_FORM_DATA, "file");
            if (CollectionUtils.isNotEmpty(dto.getInput().getParams())) {
                Map<String, Object> paramMap = utils.buildInput(dto.getInput().getParams());
                for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                    StringBody stringBody = new StringBody(entry.getValue().toString(), ContentType.create("multipart/form-data", Consts.UTF_8));
                    builder.addPart(entry.getKey(), stringBody);
                }
            }
            if (CollectionUtils.isNotEmpty(dto.getInput().getBody())) {
                Map<String, Object> bodyMap = utils.buildInput(dto.getInput().getBody());
                for (Map.Entry<String, Object> entry : bodyMap.entrySet()) {
                    StringBody stringBody = new StringBody(entry.getValue().toString(), ContentType.create("multipart/form-data", Consts.UTF_8));
                    builder.addPart(entry.getKey(), stringBody);
                }
            }
            org.apache.http.HttpEntity entity = builder.build();
            post.setEntity(entity);

            HttpResponse response = client.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            output.setSuccess(true);
            output.setStatus(statusCode);
            output.setSize((int) response.getEntity().getContentLength());
            for (Header header : response.getAllHeaders()) {
                output.getHeaders().put(header.getName(), header.getValue());
            }

            if (response.getEntity() != null) {
                String result = EntityUtils.toString(response.getEntity(), "utf-8");
//                System.out.print("http.success_request_uri:url=" + uri + ",method=" + input.getMethod() + ",code=" + statusCode + ",body=" + body + ",response=" + result);
                try {
                    output.setBody(JsonUtils.parse(result));
                } catch (Exception e) {
                    output.setBody(result);
                }

            } else {
                //log.info("http.success_request_uri:url={},method={},code={},body={},response={}",uri,input.getMethod(),statusCode,body,"");
//                System.out.print("http.success_request_uri:url=" + uri + ",method=" + input.getMethod() + ",code=" + statusCode + ",body=" + body + ",response=");

            }
            return output;

        } catch (Exception e) {

        } finally {
            if (uploadDTO.getInputStream() != null) {
                try {
                    uploadDTO.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return output;

    }

    private static UploadDTO getInputStream(HttpDebugDto dto) {
        UploadDTO uploadDTO = new UploadDTO();
        if (CollectionUtils.isNotEmpty(dto.getInput().getParams())) {
            for (JsonType param : dto.getInput().getParams()) {
                if (param.getType().equals("file")) {
                    try {
                        URL url1 = new URL(param.getValue().toString());
                        boolean Safe = jdSsrfCheck(url1);
                        if(Safe){
                            //继续执行业务
                        }else{
                            //拒绝
                        }
                        //创建连接
                        URLConnection urlConnection = url1.openConnection();
                        //获得inputStream
                        InputStream inputStream = urlConnection.getInputStream();
                        uploadDTO.setInputStream(inputStream);
                        uploadDTO.setFieldName(param.getName());
                        uploadDTO.setDownloadUrl(param.getValue().toString());
                        return uploadDTO;
                    } catch (Exception ex) {

                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(dto.getInput().getBody())) {
            for (JsonType param : dto.getInput().getBody()) {
                if (param.getType().equals("file")) {
                    try {
                        URL url1 = new URL(param.getValue().toString());
                        boolean Safe = jdSsrfCheck(url1);
                        if(Safe){
                            //继续执行业务
                        }else{
                            //拒绝
                        }
                        //创建连接
                        URLConnection urlConnection = url1.openConnection();
                        //获得inputStream
                        InputStream inputStream = urlConnection.getInputStream();
                        uploadDTO.setInputStream(inputStream);
                        uploadDTO.setFieldName(param.getName());
                        uploadDTO.setDownloadUrl(param.getValue().toString());
                        return uploadDTO;
                    } catch (Exception ex) {

                    }
                }
            }
        }

        return uploadDTO;


    }

    public static boolean jdSsrfCheck(URL urlObj){
        //定义请求协议白名单列表
        String[] allowProtocols = new String[]{"http", "https"};
        //定义请求域名白名单列表，根据业务需求进行配置
        String[] allowDomains = new String[]{"www.jd.com"};
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

    private static String getUrlFileName(String url) {
        try {
            URL url1 = new URL(url);
            return url1.getFile();

        } catch (Exception ex) {

        }
        return "";
    }

    public static JsfOutputExt invokeJarJsfCaller(JarJsfDebugDto metadata, URLClassLoader urlClassLoader)  {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        JsfOutputExt jsfOutput = new JsfOutputExt();
        Long start = System.currentTimeMillis();
        ConsumerConfig consumerConfig = null;
        try{



            Thread.currentThread().setContextClassLoader(urlClassLoader);
            try{
                urlClassLoader.loadClass(metadata.getInterfaceName());
            }catch (ClassNotFoundException e){
                throw new BizException("maven jar包里未找到类:"+metadata.getInterfaceName()+",请检查maven坐标配置是否有问题");
            }
             //JSFMsgPack.getTemplateRegistry().setClassLoader(urlClassLoader);
             consumerConfig = buildConsumerConfig(metadata,false);

            Object obj = consumerConfig.refer();
            Method method = getMethod(obj, metadata.getMethodName());
            Guard.notEmpty(method,"方法不存在:"+metadata.getMethodName());
            method.setAccessible(true);
            List inputParam = (List) parseJarJsfCallParam(metadata.getInputData(),method);

            Object result = method.invoke(obj, inputParam.toArray());
            jsfOutput.setSuccess(true);
            jsfOutput.setBody(result);
            jsfOutput.setTime(System.currentTimeMillis() - start);
            jsfOutput.setStatus(200);
            return jsfOutput;
        }catch (Throwable e){
            log.error("jsf.err_call_jsf:metadata={}", metadata, e);
            jsfOutput.setSuccess(false);
            jsfOutput.setStatus(0);

            if(e instanceof java.lang.reflect.InvocationTargetException){
                e = e.getCause();
            }
            if(e instanceof Exception){
                jsfOutput.setException((Exception)e);
            }else{
                jsfOutput.setException(new RuntimeException(e));
            }

            return jsfOutput;
        }finally {
            if(consumerConfig != null){
                consumerConfig.unrefer();
            }
            Thread.currentThread().setContextClassLoader(contextClassLoader);

        }

    }
    private static Method getMethod(Object obj, String methodName){
        for (Method declaredMethod : obj.getClass().getDeclaredMethods()) {
            if(declaredMethod.getName().equals(methodName)){
                return declaredMethod;
            }
        }
        return null;
    }
    private static Object parseJarJsfCallParam(String json,Method method){
        Guard.notEmpty(json,"入参不可为空");
        json = json.trim();
        if(json.startsWith("{") && json.endsWith("}")){
            json = "["+json+"]";
        }
        if(!json.startsWith("[")){
            throw new BizException("入参必须为数组类型");
        }
        ParserConfig parserConfig = new ParserConfig();
        parserConfig.setAutoTypeSupport(true);
        ///ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        parserConfig.addAccept("com.jd");
        parserConfig.addAccept("com.jdh");
        parserConfig.addAccept("com.jdl");
        parserConfig.setDefaultClassLoader(Thread.currentThread().getContextClassLoader());
        // gson反序列化
        Object obj = JSON.parseArray(json,method.getGenericParameterTypes(),parserConfig);
        return obj;
    }
    private static RegistryConfig getRegistry(String env) {
        RegistryConfig jsfRegistry = new RegistryConfig();
        if (StringUtils.isNotBlank(env)) {
            final JsfRegistryEnvEnum registryEnum = JsfRegistryEnvEnum.from(env);
            jsfRegistry.setIndex(registryEnum.getAddress()); // 测试环境192.168.150.121 i.jsf.jd.com
        }
        return jsfRegistry;
    }


    private static ConsumerConfig buildConsumerConfig(JarJsfDebugDto metadata,boolean generic){
        // 服务消费者连接注册中心，设置属性
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setInterfaceId(metadata.getInterfaceName());// 这里写真实的类名
        consumerConfig.setRegistry(getRegistry(metadata.getEnv()));
        consumerConfig.setProtocol("jsf");
        consumerConfig.setSerialization("hessian");
        // consumerConfig.setUrl(metadata.getUrl());
        consumerConfig.setTimeout(5000);
        consumerConfig.setAlias(metadata.getAlias());
        if (JsfRegistryEnvEnum.local.name().equals(metadata.getEnv())) {
            String ipPort;
            if (StringUtils.isEmpty(ipPort = metadata.getIp()) && StringUtils.isEmpty(metadata.getAlias())) {
                throw new BizException("本地调试必须填Ip或者别名");
            }
            consumerConfig.setParameter("proxy.enableProxy", "true");
            if (StringUtils.isNotEmpty(ipPort)) {
                ipPort = ipPort.matches(".+:.+") ? ipPort : (ipPort + ":22000");
                String callUrl = "jsf://" + ipPort + "?alias=" + metadata.getAlias();
                consumerConfig.setUrl(callUrl);

            }
            consumerConfig.setParameter("proxy.enableProxy", "true");
        }else if(StringUtils.isNotEmpty(metadata.getIp())){
            String callUrl = "jsf://" + metadata.getIp() + "?alias=" + metadata.getAlias();
            consumerConfig.setUrl(callUrl);
        }
        consumerConfig.setParameter(".warnning", "false");
        //consumerConfig.setLazy(true);
        consumerConfig.setGeneric(generic); // 需要指定是Generic调用true
        consumerConfig.setTimeout(5 * 60 * 1000);
        // consumerConfig.setAsync(true); // 如果异步
        if (metadata.getAttachments() != null) {
            Map<String, String> params = new HashMap<>();
            for (JsonType attachment : metadata.getAttachments()) {
                if(attachment == null){
                    continue;
                }
                params.put(attachment.getName(), Variant.valueOf(attachment.getValue()).toString());
            }

            consumerConfig.getParameters().putAll(params);
        }
        return consumerConfig;
    }
    private static ConsumerConfig buildConsumerConfig(NewJsfDebugDto metadata,boolean generic){
        RegistryConfig jsfRegistry = getRegistry(metadata.getEnv());
        // 服务消费者连接注册中心，设置属性
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setInterfaceId(metadata.getInterfaceName());// 这里写真实的类名
        consumerConfig.setRegistry(jsfRegistry);
        consumerConfig.setProtocol("jsf");
        // consumerConfig.setUrl(metadata.getUrl());
        consumerConfig.setTimeout(5000);
        consumerConfig.setAlias(metadata.getAlias());
        if (JsfRegistryEnvEnum.local.name().equals(metadata.getEnv())) {
            String ipPort;
            if (StringUtils.isEmpty(ipPort = metadata.getIp()) && StringUtils.isEmpty(metadata.getAlias())) {
                throw new BizException("本地调试必须填Ip或者别名");
            }
            consumerConfig.setParameter("proxy.enableProxy", "true");
            if (StringUtils.isNotEmpty(ipPort)) {
                ipPort = ipPort.matches(".+:.+") ? ipPort : (ipPort + ":22000");
                String callUrl = "jsf://" + ipPort + "?alias=" + metadata.getAlias();
                consumerConfig.setUrl(callUrl);

            }
            consumerConfig.setParameter("proxy.enableProxy", "true");
        }else if(StringUtils.isNotEmpty(metadata.getIp())){
            String callUrl = "jsf://" + metadata.getIp() + "?alias=" + metadata.getAlias();
            consumerConfig.setUrl(callUrl);
        }
        consumerConfig.setParameter(".warnning", "false");


        //consumerConfig.setLazy(true);
        consumerConfig.setGeneric(generic); // 需要指定是Generic调用true
        consumerConfig.setTimeout(5 * 60 * 1000);
        // consumerConfig.setAsync(true); // 如果异步
        if (metadata.getAttachments() != null) {
            Map<String, String> params = new HashMap<>();
            for (JsonType attachment : metadata.getAttachments()) {
                if(attachment == null){
                    continue;
                }
                params.put(attachment.getName(), Variant.valueOf(attachment.getValue()).toString());
            }

            consumerConfig.getParameters().putAll(params);
        }
        return consumerConfig;
    }

    public static JsfOutput callJsf(NewJsfDebugDto metadata) {
        ConsumerConfig consumerConfig = buildConsumerConfig(metadata,true);
        // 得到泛化调用实例，此操作很重，请缓存consumerConfig或者service对象！！！！（用map或者全局变量）
        GenericService genericService = (GenericService) consumerConfig.refer();

        List paramValue = metadata.getInputData();
        if (paramValue == null) {
            paramValue = new ArrayList();
        }
        if (ReqType.form.name().equals(metadata.getReqType()) || metadata.getInputData() == null  ) {
            Map<String, Object> extArgs = new HashMap<>();
            extArgs.put("input", new HashMap<>());
            ParamMappingContext paramMappingContext = new ParamMappingContext(new StepContext(), extArgs);
            Map<String, Object> inputValues = utils.getJsonInputValue(metadata.getInput(), paramMappingContext);
            for (JsonType jsonType : metadata.getInput()) {
                Object value = inputValues.get(jsonType.getName());
                Object convertValue = JsfParamConverterRegistry.convertValue(jsonType, value);
                paramValue.add(convertValue);

            }
        }
        metadata.setInputData(paramValue);
        try {
            Object result = genericService.$invoke(metadata.getMethodName(), getParamTypes(metadata), paramValue.toArray(new Object[0]));
            JsfOutput output = new JsfOutput();
            output.setSuccess(true);
            output.setBody(result);
            return output;
        } catch (RuntimeException e) {
            log.error("jsf.err_call_jsf:metadata={}", metadata, e);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(os));
            JsfOutput output = new JsfOutput();
            output.setSuccess(false);
            output.setException(e);
            return output;
        } finally {
            consumerConfig.unrefer();
        }

    }

    private static String[] getParamTypes(NewJsfDebugDto metadata) {
        if(metadata.getParamTypes() != null ){
            return metadata.getParamTypes().toArray(new String[0]);
        }
        List<? extends JsonType> jsonTypes = metadata.getInput();
        String[] paramTypes = new String[jsonTypes.size()];
        for (int i = 0; i < jsonTypes.size(); i++) {
            JsonType jsonType = jsonTypes.get(i);
            if (jsonType.isRequired() && jsonType instanceof SimpleJsonType) {

            }
            paramTypes[i] = jsonType.getClassName();
        }
        return paramTypes;
    }

    public static String getHostFromUrl(String requestUrl) {
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

}
