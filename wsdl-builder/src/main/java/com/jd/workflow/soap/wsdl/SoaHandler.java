/*
package org.apache.camel.example.wsdl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 类似于J2EE中的Service类， 只是增删改查操作已经被Dao统一实现， 不需要在此类中增加这些函数。
public class SoaHandler {
    private Logger LOG = LoggerFactory
            .getLogger(SoaHandler.class);

    public boolean existUrl(String resourceUrl, int connectTimeOut) {
        URL url = null;
        try {
            url = new URL(resourceUrl);
        } catch (MalformedURLException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
            LOG.error("invalid url:" + resourceUrl, e);
        }
        return exists(url, connectTimeOut);
    }

    */
/**
     * 测试url是否合法
     *
     * @param url
     * @param connectTimeOut 超时时间 毫秒
     * @return
     *//*

    public boolean exists(URL url, int connectTimeOut) {
        InputStream localInputStream = null;
        try {
            LOG.info("begin open connection::");
            URLConnection connection = url.openConnection();
            LOG.info("end open connection::");
            connection.setConnectTimeout(connectTimeOut);
            connection.setReadTimeout(connectTimeOut);
            LOG.info("begin read inputStream::");
            localInputStream = connection.getInputStream();
            LOG.info("end read inputStream::");
            return true;
        } catch (Exception localException) {
            LOG.error("invalid url:" + url, localException);
            return false;
        } finally {
            IOUtils.closeQuietly(localInputStream);
        }
    }
//	public void saveWsdlUrlContent(String wsdlUrl) {
//		String localFile = Config.sysParam("wsdl.save_local_file").stringValue("D:/whf/wsdl.wsdl");
//		String contextFile = Config.var("app.fs.rootPath").stringValue("D:/");
//		contextFile = contextFile + "wsdl.wsdl";
//		URL urlFile = null;
//		HttpURLConnection httpUrl = null;
//		BufferedInputStream bis = null;
//		BufferedOutputStream bos = null;
//		File f = new File(localFile);
//		try {
//			urlFile = new URL(wsdlUrl);
//			httpUrl = (HttpURLConnection) urlFile.openConnection();
//			httpUrl.connect();
//			bis = new BufferedInputStream(httpUrl.getInputStream());
//			bos = new BufferedOutputStream(new FileOutputStream(f));
//			int len = 2048;
//			byte[] b = new byte[len];
//			while ((len = bis.read(b)) != -1) {
//				bos.write(b, 0, len);
//			}
//			bos.flush();
//			bis.close();
//			httpUrl.disconnect();
//			
//			String text = FileUtils.loadText(new File(localFile));
//			ContextHelper.saveText(contextFile, text);
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				bis.close();
//				bos.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}

    public String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public Map<String, Object> isUrlValid(String url) {
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("isUrlValid", false);
        try {
            validateWsdl(url);
            ret.put("isUrlValid", true);
        } catch (WSDLException e) {
            e.printStackTrace();
            throw new RuntimeException("soa.CAN_err_invalid_wsdl_content");
        } catch (MalformedURLException e) {
            throw new RuntimeException("soa.CAN_err_invalid_wsdl_address");
        } catch (RuntimeException e) {
            throw e;
        } catch (ConnectException e) {
            throw new RuntimeException("soa.CAN_err_invalid_connect");
        } catch (IOException e) {
            e.printStackTrace();
            ret.put("desc", "url无效");
            ret.put("isUrlValid", false);
        } catch (Exception e) {
            ret.put("desc", "url无效");
            e.printStackTrace();
            ret.put("isUrlValid", false);
        }

        return ret;
    }

    public ServiceInfo getWsdlInfo(String wsdlUrl) {
        Definition definition = null;
        WSDLReader reader = null;
        try {
            validateWsdl(wsdlUrl);
            reader = WSDLFactory.newInstance().newWSDLReader();
            definition = reader.readWSDL(wsdlUrl);
        } catch (Exception e) {
            // TODO 自动生成的 catch 块
            LOG.error("error occur when read wsdl!", e);
            return null;
        }
        Client client = null;
        try {
            JaxWsDynamicClientFactory factory = JaxWsDynamicClientFactory.newInstance();
            client = factory.createClient(wsdlUrl);
        } catch (Exception e) {
            LOG.error("error occur when read wsdl!", e);
            return null;
        }

        ServiceInfo info = client.getEndpoint().getEndpointInfo().getService();
        // Map portTypes =  definition.getPortTypes();
        return info;
    }



    public static void translateBindIngOpeationInfoToApiMethod(BindingOperationInfo info) {
        QName name = info.getName();
        System.out.println("method name:" + name);
        BindingMessageInfo messageInfo = info.getInput();
        List<MessagePartInfo> messagePartInfos = messageInfo.getMessageParts();
        for (MessagePartInfo messagePartInfo : messagePartInfos) {
            System.out.println(messagePartInfo);
        }
    }

    public static void main(String[] args) throws WSDLException {

    }

    public List<SoaApiMethod> generateServiceMethod(String wsdlUrl) {
        CxfWsModelBuilder model = new CxfWsModelBuilder();
        ServiceInfo serviceInfo = getWsdlInfo(wsdlUrl);
        List<SoaApiMethod> apiMethods = new ArrayList<SoaApiMethod>();
        if (serviceInfo != null) {
            WsModel wsModel = model.buildFromServiceInfo(serviceInfo);
            List<WsOperationModel> optionModels = wsModel.getOperations();
            for (WsOperationModel optionModel : optionModels) {
                apiMethods.add(transformWsOperationModelToApiParameter(optionModel));
            }
        }
        return apiMethods;
    }

    public SoaServiceInfo generateServiceInfoByWsdl(String wsdl, SoaServiceInfo src) {
        SoaServiceInfo info = new SoaServiceInfo();
        ;
        DaoHandler daoHandler = DaoHandler.getInstance();
        if (src != null) {
            src.copyTo(info, false);
            info.setSid(null);
        }
        List<SoaApiMethod> apiMethods = generateServiceMethod(wsdl);
        if (apiMethods.isEmpty()) {
            return null;
        }
        info.setServiceType(SoaConstants.SERVICE_TYPE_SOAP);
        info.getSoaApiMethods().addAll(apiMethods);
        for (SoaApiMethod apiMethod : apiMethods) {
            apiMethod.setSoaServiceInfo(info);
        }
        info.setWsdlUrl(wsdl);
        return info;
    }

    public SoaServiceInfo generateServiceInfoByWsdl(String wsdl) {
        return generateServiceInfoByWsdl(wsdl, null);
    }

    public SoaApiMethod transformWsOperationModelToApiParameter(WsOperationModel model) {
        if (model == null) {
            return null;
        }
        SoaApiMethod method = new SoaApiMethod();
        method.setResourceUrl(model.getName());
        method.setHttpMethod(SoaConstants.HTTP_METHOD_POST);
        method.setNamespaceUri(model.getNamespaceURI());
        method.setDescription(model.getDescription());
        if (model.getShowName() == null) {
            method.setShowName(model.getName());
        } else {
            method.setShowName(model.getShowName());
        }
        List<WsArgumentModel> arguments = model.getArguments();
        for (WsArgumentModel argument : arguments) {
            SoaApiMethodParameter parameter = new SoaApiMethodParameter();
            parameter.setParameterName(argument.getName());
            //parameter.setParameterType(argument.getTypeString());
            int i = 0;
            Class type = argument.getBaseType().getTypeClass();
            if (argument.isArray()) {
                parameter.setDataType("array");
                String name = "";
                if (argument.getBaseType().getTypeClass().getComponentType() != null) {
                    name = argument.getBaseType().getTypeClass().getComponentType().getName();
                    name = name.substring(name.lastIndexOf(".") + 1);
                }
                parameter.setComponentType(name);
            } else {
                parameter.setDataType(argument.getTypeString().toLowerCase());
            }
            parameter.setXh(i++);
            method.getSoaApiMethodParameters().add(parameter);
            parameter.setMethod(method);
        }

        return method;

    }

    public void validateWsdl(String url) throws IOException, WSDLException {
        if (StringUtils.isEmpty(url)) {
            throw new RuntimeException("URL is not empty");
        }
        URL wsdl = new URL(url);
        BufferedReader in = new BufferedReader(new InputStreamReader(wsdl.openStream(), Charset.defaultCharset()));

        boolean isWsdl2 = false;
        boolean isWsdl10 = false;
        StringBuilder urlContent = new StringBuilder();
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String wsdl2NameSpace = "http://www.w3.org/ns/wsdl";
                String wsdl10NameSpace = "http://schemas.xmlsoap.org/wsdl/";
                urlContent.append(inputLine);
                isWsdl2 = urlContent.indexOf(wsdl2NameSpace) > 0;
                isWsdl10 = urlContent.indexOf(wsdl10NameSpace) > 0;
            }
        } finally {
            in.close();
        }
        if (isWsdl10) {
            WSDLReader wsdlReader11 = WSDLFactory.newInstance().newWSDLReader();
            wsdlReader11.readWSDL(url);
        }
		    */
/*else if (isWsdl2)
		    {
		      org.apache.woden.WSDLReader wsdlReader20 = org.apache.woden.WSDLFactory.newInstance().newWSDLReader();
		      wsdlReader20.readWSDL(url);
		    }*//*

        else {
            throw new RuntimeException("soa.CAN_err_invalid_wsdl_content");
        }
    }

    public String getIpAddress() {
        InetAddress address;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
            return "";
        }
        String ip = address.getHostAddress();
        return ip;
    }




    private void parsePaths(SoaServiceInfo serviceInfo,
                            Object pathsData) {
        // TODO 自动生成的方法存根
        if (!(pathsData instanceof Map)) {
            LOG.error("swagger path is not a map data");
            return;
        }
        Map<String, Object> data = (Map<String, Object>) pathsData;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String path = entry.getKey();
            LOG.info("------begin parsed path :: {}-------", path);
            Object value = entry.getValue();
            parseMethods(value, path, serviceInfo);
            LOG.info("------end parsed path :: {}-------", path);
        }
    }

    private void parseMethods(Object methodData, String path, SoaServiceInfo info) {
        // TODO 自动生成的方法存根
        if (!(methodData instanceof Map)) {
            LOG.error("swagger paths.path:{} is not a valid map!", methodData);
            return;
        }
        Map<String, Object> v = (Map<String, Object>) methodData;
        for (Map.Entry<String, Object> entry : v.entrySet()) {
            String httpMethod = entry.getKey();
            Object value = entry.getValue();
            SoaApiMethod method = parseMethodValue(value, httpMethod, path);
            if (method != null) {
                info.getSoaApiMethods().add(method);
                method.setSoaServiceInfo(info);
                LOG.info("path:{} method:{}{} has bean parsed!", path, method.getHttpMethod(), method.getResourceUrl());
            }
        }
    }

    private SoaApiMethod parseMethodValue(Object methodValueData, String httpMethod, String resourceUrl) {
        // TODO 自动生成的方法存根
        if (!(methodValueData instanceof Map || methodValueData == null)) {
            LOG.error("swagger path methodValueData is not a valid Map!");
            return null;
        }
        SoaApiMethod apiMethod = new SoaApiMethod();
        Map<String, Object> map = (Map<String, Object>) methodValueData;
        try {
            apiMethod.setDescription((String) map.get("description"));
            apiMethod.setShowName((String) map.get("summary"));
            List<String> consumes = (List<String>) map.get("consumes");
            List<String> produces = (List<String>) map.get("produces");
            apiMethod.setConsumes(getConsumeOrProduces(consumes));
            apiMethod.setProduces(getConsumeOrProduces(produces));
            apiMethod.setHttpMethod(httpMethod);
            apiMethod.setResourceUrl(resourceUrl);
        } catch (Exception e) {
            LOG.error("error happened!", e);
        }
        Object parameters = map.get("parameters");
        if (!(parameters instanceof List)) {
            LOG.error("patameters is not a valid array" + toString(parameters));
        }
        List<Object> list = (List<Object>) parameters;
        for (Object o : list) {
            parseMethodParameter(o, apiMethod);
        }
        return apiMethod;
    }

    public String getConsumeOrProduces(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        IEnumInfo info = EnumLoader.loadEnum("content_type.enum.xml", null);
        for (String s : list) {
            if (info.getItem(s) != null) {
                return s;
            }
        }
        return null;
    }

    private void parseMethodParameter(Object o, SoaApiMethod apiMethod) {
        // TODO 自动生成的方法存根
        if (!(o instanceof Map)) {
            LOG.error("swagger method parameter is not a valid Map" + o);
            return;
        }
        Map<String, Object> map = (Map<String, Object>) o;
        SoaApiMethodParameter param = null;
        try {
            param = new SoaApiMethodParameter();
            param.setParameterType((String) map.get("in"));
            //param.setDataType((String) map.get("dataType"));
            if ((Boolean) map.get("required") == true) {
                param.setRequired("Y");
            } else {
                param.setRequired("N");
            }

            param.setParameterName((String) map.get("name"));
            param.setDescription((String) map.get("description"));
            String type = (String) map.get("type");
            if (!StringUtilsEx.isEmpty(type)) {
                param.setDataType(type);
            } else {
                Object schemas = map.get("schema");
                if (schemas != null) {
                    param.setDataType("object");
                }
            }
            apiMethod.getSoaApiMethodParameters().add(param);
            param.setMethod(apiMethod);
            LOG.info("parsed parameter::name:{} in:{} type:{}  for method::{}{}", (String) map.get("name"), (String) map.get("in"), param.getDataType(), apiMethod.getHttpMethod(), apiMethod.getResourceUrl());
        } catch (ClassCastException e) {
            LOG.error("error happened!", e);
        }
    }

    String toString(Object s) {
        if (s == null) {
            return null;
        }
        if (s instanceof Collection) {
            return ((Collection) s).toString();
        } else if (s.getClass().isArray()) {
            return Arrays.toString((Object[]) s);
        } else if (s instanceof Map) {
            return ((Map) s).toString();
        } else {
            return s.toString();
        }
    }

    private void parseSchema(SoaServiceInfo serviceInfo, Object data) {
        // TODO 自动生成的方法存根
        if (!(data instanceof String[] || data instanceof List)) {
            LOG.warn("schemas {} is not a String[] or List Object!", data);
        }
        List<String> datas = (List) (data);
        List<String> result = new ArrayList<String>();
        for (String s : datas) {
            if (s.equalsIgnoreCase(SoaConstants.HTTP_SCHEMA_HTTP) || s.equalsIgnoreCase(SoaConstants.HTTP_SCHEMA_HTTPS)) {
                result.add(s);
            }
        }
        LOG.info("parsed schema:{}", StringUtilsEx.join(result, ","));
        serviceInfo.setHttpSchemas(StringUtilsEx.join(result, ","));
    }

    private void parseInfo(SoaServiceInfo info, Object infoData) {
        // TODO 自动生成的方法存根
        if (!(infoData instanceof Map)) {
            LOG.info("swagger info is not a valid map!");
            return;
        }
        try {
            Map data = (Map) infoData;
            info.setDescription((String) data.get("description"));
            info.setVersion((String) data.get("version"));
            info.setServiceName((String) data.get("title"));
            LOG.info("parsed info title:{} descrition:{} version:{}", (String) data.get("title"), (String) data.get("description"), (String) data.get("version"));
        } catch (ClassCastException e) {
            LOG.error("error happened!", e);
        }

    }

    public boolean validateSwaggerFile(String jsonData) {
        String[] keyWords = new String[]{"swagger", "info", "paths", "version"};
        for (String keyWord : keyWords) {
            if (jsonData.indexOf(keyWord) == -1) {
                LOG.error("swagger keyword {} not found! invalid swaggerFile!", keyWord);
                return false;
            }
        }
        return true;
    }

    public SoaServiceInfo newSoaServiceInfo() {
        return new SoaServiceInfo();
    }

    public static final long YEAR = 365 * 24 * 60 * 60 * 1000L;
    public static final long MONTH = 30 * 24 * 60 * 60 * 1000L;
    public static final long DAY = 24 * 60 * 60 * 1000L;
    public static final long HOUR = 60 * 60 * 1000L;
    public static final long MINUTE = 60 * 1000L;
    public static final long SECOND = 1000L;
    public static final String YEAR_FORMAT = "%Y";
    public static final String YEAR_MONTH_FORMAT = "%Y %m";
    public static final String YEAR_MONTH_DAY_FORMAT = "%Y-%m-%d";
    public static final String YEAR_MONTH_DAY_H_FORMAT = "%Y-%m-%d %H";
    public static final String YEAR_MONTH_DAY_H_M_FORMAT = "%Y-%m-%d %H:%i";
    public static final String YEAR_MONTH_DAY_H_M_S_FORMAT = "%Y-%m-%d %H:%i:%s";

    */
/**
     * 根据开始时间、结束时间获取要统计的区间
     *
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param minInterval 最小间隔，当时间差小于此的时候低一个区间统计
     * @return
     *//*

    public String getFormat(Timestamp startTime, Timestamp endTime, Integer minInterval) {
        if (minInterval == null) {
            minInterval = 2;
        }
        if (startTime == null) {
            return YEAR_FORMAT;
        }
        if (endTime == null) {
            endTime = new Timestamp(System.currentTimeMillis());
        }
        long millSecond = endTime.getTime() - startTime.getTime();
        if (millSecond / YEAR > minInterval) {
            return YEAR_FORMAT;
        } else if (millSecond / MONTH > minInterval) {
            return YEAR_MONTH_FORMAT;
        } else if (millSecond / DAY > minInterval) {
            return YEAR_MONTH_DAY_FORMAT;
        } else if (millSecond / HOUR > minInterval) {
            return YEAR_MONTH_DAY_H_FORMAT;
        } else if (millSecond / MINUTE > minInterval) {
            return YEAR_MONTH_DAY_H_M_FORMAT;
        } else {
            return YEAR_MONTH_DAY_H_M_S_FORMAT;
        }
    }

    public long getTimeInterval(String sqlFormatString) {
        Map<String, Long> map = new HashMap<String, Long>();
        map.put(YEAR_MONTH_DAY_H_M_S_FORMAT, SECOND);
        map.put(YEAR_MONTH_DAY_H_M_FORMAT, MINUTE);
        map.put(YEAR_MONTH_DAY_H_FORMAT, HOUR);
        map.put(YEAR_MONTH_DAY_FORMAT, DAY);
        map.put(YEAR_MONTH_FORMAT, MONTH);
        map.put(YEAR_FORMAT, YEAR);
        return map.get(sqlFormatString);
    }

    public String getFormat(String sqlFormatString) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(YEAR_MONTH_DAY_H_M_S_FORMAT, "yyyy-MM-dd HH:mm:ss");
        map.put(YEAR_MONTH_DAY_H_M_FORMAT, "yyyy-MM-dd HH:mm");
        map.put(YEAR_MONTH_DAY_H_FORMAT, "yyyy-MM-dd HH");
        map.put(YEAR_MONTH_DAY_FORMAT, "yyyy-MM-dd");
        map.put(YEAR_MONTH_FORMAT, "yyyy-MM");
        map.put(YEAR_FORMAT, "yyyy");
        return map.get(sqlFormatString);
    }

    */
/**
     * 补全historyInfo信息，[{timeInterval:2016-12-1 12:22:xx,count:1},{timeInterval:2016-12-1,count:2},{timeInterval:2016-12,count:1}]
     * 补全缺少的数据，如startTime与endTime之间差2小时，则如果historyInfos不是每分钟的数据，则自动将每分钟的数据补全，该方式存在问题，开始时间与结束时间存在差异
     *
     * @param historyInfos
     * @param formatString
     * @param showTraceResult
     * @return
     *//*

    public List<Map<String, Object>> fixHistoryInfo(List<Map<String, Object>> historyInfos, Timestamp startTime, Timestamp endTime, String traceResult) {
        String sqlFormatString = getFormat(startTime, endTime, 3);
        String formatString = getFormat(sqlFormatString);
        List<Map<String, Object>> infos = new ArrayList<Map<String, Object>>();
		*/
/*if(historyInfos.isEmpty()){//无数据也显示？
			return infos;
		}*//*

        String startTimeFormatString = StringUtilsEx.formatDate(startTime, formatString);
        String endTimeFormatString = StringUtilsEx.formatDate(endTime, formatString);
        long timeInterval = getTimeInterval(sqlFormatString);//获取每个时间间隔点
        long start = EasyCalendar.fromString(startTimeFormatString).getTimeInMillis();
        long end = EasyCalendar.fromString(endTimeFormatString).getTimeInMillis();
        while (start <= end) {
            String formatDate = StringUtilsEx.formatDate(new Timestamp(start), formatString);
            Map<Object, Object> historyInfo = new WrapCollection(historyInfos).toMap("timeInterval", "count");

            Object count = historyInfo.get(formatDate);
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("timeInterval", formatDate);
            if (count == null) {
                result.put("count", 0);
                if (!StringUtilsEx.isEmpty(traceResult)) {
                    result.put("isSuccess", "traceResult");
                }
            } else {
                result.put("count", count);
                if (!StringUtilsEx.isEmpty(traceResult)) {
                    result.put("isSuccess", "traceResult");
                }
            }
            infos.add(result);
            start = start + timeInterval;
        }
        return infos;
    }

    */
/**
     * 根据format自动补全缺失的数据
     *
     * @param historyInfos
     * @param startTime
     * @param endTime
     * @param sqlFormatString
     * @return
     *//*

    public List<Map<String, Object>> fixHistoryInfoByFormat(List<Map<String, Object>> historyInfos, Timestamp startTime, Timestamp endTime, String sqlFormatString, List<String> clientIps) {
        String formatString = getFormat(sqlFormatString);
        List<Map<String, Object>> infos = new ArrayList<Map<String, Object>>();
        String startTimeFormatString = StringUtilsEx.formatDate(startTime, formatString);
        String endTimeFormatString = StringUtilsEx.formatDate(endTime, formatString);
        long timeInterval = getTimeInterval(sqlFormatString);//获取每个时间间隔点
        long start = EasyCalendar.fromString(startTimeFormatString).getTimeInMillis();
        long end = EasyCalendar.fromString(endTimeFormatString).getTimeInMillis();
        while (start <= end) {
            String formatDate = StringUtilsEx.formatDate(new Timestamp(start), formatString);

            for (int i = 0; i < clientIps.size(); i++) {
                String clientIp = clientIps.get(i);
                int found = -1;
                for (int j = 0; j < historyInfos.size(); j++) {
                    Map<String, Object> historyInfo = historyInfos.get(j);
                    if (clientIp.equals(historyInfo.get("clientIp")) && formatDate.equals(historyInfo.get("timeInterval"))) {
                        found = j;
                        infos.add(historyInfo);
                        historyInfos.remove(j);
                        break;
                    }
                }
                if (found == -1) {//未找到任何数据，直接使用空数据进行补充
                    Map<String, Object> emptyData = new HashMap<String, Object>();
                    emptyData.put("clientIp", clientIp);
                    emptyData.put("timeInterval", formatDate);
                    emptyData.put("count", 0);
                    infos.add(emptyData);
                }
            }
            start = start + timeInterval;
        }
        return infos;
    }

    public List<Map<String, Object>> fixHistoryInfo(List<Map<String, Object>> historyInfos, Timestamp startTime, Timestamp endTime) {
        return fixHistoryInfo(historyInfos, startTime, endTime, null);
    }

    public IUserContext getUserManager(String userId) {
        return UserManager.getUserContext(userId);
    }

    public Map<String, IUserContext> getActiveUsers() {
        return UserManager.getActiveUsers(null);
    }

    public void logout(HttpSession session) {
        WebEngine.getInstance().logoutWebSession(session, 1);
    }

    public Object removeWebEntity(String entityId, String objectName, Map<String, Object> vars, WebContext context) {
        WebInvocation inv = new WebInvocation(context);
        inv.objectName(objectName);
        inv.inheritParams(false);
        inv.inheritPersist(false);

        if (vars == null)
            vars = new HashMap();
        return inv.callRemove(entityId);
    }

    public static String getServerAddress(HttpServletRequest req) {
        if (req == null) {
            return null;
        }
        String scheme = req.getScheme();
        String localAddr = req.getLocalAddr();
        if ("0:0:0:0:0:0:0:1".equals(localAddr)) {
            localAddr = "127.0.0.1";
        }
        int localPort = req.getLocalPort();
        if (-1 == localPort) {
            localPort = 80;
        }
        return scheme + "://" + localAddr + ":" + localPort + req.getContextPath();

    }

    public static String getHttpsServerAddress(HttpServletRequest req) {
        String serverUrl = (String) AppConfig.getVar("oauth.server_url");
        if (!StringUtilsEx.isEmpty(serverUrl)) {
            return serverUrl;
        }
        String httpsAddress = (String) req.getServletContext().getAttribute(SoaConstants.WEBSITE_HTTPS_ADDRESS_KEY);
        if (httpsAddress != null) {
            return httpsAddress;
        } else {
            throw new RuntimeException("oauth.CAN_err_oauth.server_url_is_null");
        }


    }

    public static String getHttpServerAddress(HttpServletRequest req) {
        Object httpServerAddress = AppConfig.getVar("web.server_http_address");
        if (httpServerAddress != null) {
            return httpServerAddress.toString();
        }
        Object address = req.getServletContext().getAttribute(SoaConstants.WEBSITE_HTTP_ADDRESS_KEY);
        if (address != null) {
            return address.toString();
        } else {
            Object port = AppConfig.getVar("web.defaultPort");
            if (port == null) {
                port = "8010";
            }
            return getHttpServerAddress(req, port.toString());
        }
    }

    public static String getHttpServerAddress(HttpServletRequest req, String defaultPort) {
        if (req == null) {
            return null;
        }
        String scheme = req.getScheme();
        String localAddr = req.getLocalAddr();
        if ("0:0:0:0:0:0:0:1".equals(localAddr)) {
            localAddr = "127.0.0.1";
        }
        int localPort = req.getLocalPort();
        if (-1 == localPort) {
            localPort = 80;
        }
        if ("http".equals(scheme)) {
            return scheme + "://" + localAddr + ":" + localPort + req.getContextPath();
        } else {
            return "http://" + localAddr + ":" + defaultPort + req.getContextPath();
        }

    }

    public static String getServiceAddress(HttpServletRequest req, String serviceName, String serviceVersion) {
        return getServiceAddress(req, serviceName, serviceVersion, SoaConstants.SERVICE_NOT_DEFAULT_VERSION);
    }

    public static String getServiceAddress(HttpServletRequest req, String serviceName, String serviceVersion, String isDefaultVersion) {
        String url = "";
        String gatewayport = (String) AppConfig.getVar("gateway.service_port");
        if (StringUtils.isEmpty(gatewayport)) {
            gatewayport = "5121";
        }
        String localAddr = req.getLocalAddr();
        if ("0:0:0:0:0:0:0:1".equals(localAddr)) {
            localAddr = "127.0.0.1";
        }
        int localPort = req.getLocalPort();
        if (-1 == localPort) {
            localPort = 80;
        }
        if (isDefaultVersion.equalsIgnoreCase(SoaConstants.SERVICE_DEFAULT_VERSION)) {
            url = "http://" + localAddr + ":" + gatewayport + "/" + serviceName + "/";
        } else {
            url = "http://" + localAddr + ":" + gatewayport + "/" + serviceName + "/v" + serviceVersion + "/";
        }

        return url;
    }

    public static String getWsdlLocation(HttpServletRequest req, String serviceName, String serviceVersion) {
        return getWsdlLocation(req, serviceName, serviceVersion, SoaConstants.SERVICE_NOT_DEFAULT_VERSION);
    }

    public static String getWsdlLocation(HttpServletRequest req, String serviceName, String serviceVersion, String isDefaultVersion) {
        String url = getHttpServerAddress(req);
        if (SoaConstants.SERVICE_DEFAULT_VERSION.equalsIgnoreCase(isDefaultVersion)) {
            url = url + "/wsdl/" + serviceName + "/";
        } else {
            url = url + "/wsdl/" + serviceName + "/v" + serviceVersion + "/";
        }

        return url;
    }


    public String getErrorMessage(RuntimeException exception) {
        String errorMessage = "";
        edu.thu.lang.exceptions.ErrorInfo errInfo = ErrorMessages.buildErrorInfo(exception);
        if (errInfo.getErrorCode() != null)
            errorMessage = errInfo.getFormattedMessage();
        return errorMessage;
    }

    public static int getExceptionAccessStandardValue() {
        return WxSysHandler.getInstance().getSysParamNoCache(SoaConstants.WX_SYS_PARAM_EXCEPTION_ACCESS_STANDARD_VALUE_ID).intValue(100);
    }

    */
/**
     * 得到用户的状态
     *
     * @return 注册用户的状态map
     *//*

    public Map<String, Object> getUserStatus(boolean isAdmin) {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> userStatusMap = new HashMap<String, Object>();
        map.put("isAdmin", isAdmin);

        //注册用户数量
        Long registerUserNum = (Long) this.findOneBySql("oauth.getUserNumByUserStatus", map);
        //正常用户数量
        map.put("userStatus", "1");
        Long availUserNum = (Long) this.findOneBySql("oauth.getUserNumByUserStatus", map);
        //禁用用户数量
        map.put("userStatus", "2");
        Long forbiddenUserNum = (Long) this.findOneBySql("oauth.getUserNumByUserStatus", map);
        //通过邀请注册
        map.put("userStatus", null);
        map.put("registerMethod", "10");
        Long registerByInvite = (Long) this.findOneBySql("oauth.getUserNumByUserStatus", map);
        //通过统一注册 
        map.put("registerMethod", "30");
        Long registerByport = (Long) this.findOneBySql("oauth.getUserNumByUserStatus", map);
        //通过导入注册 
        map.put("registerMethod", "40");
        Long registerByImport = (Long) this.findOneBySql("oauth.getUserNumByUserStatus", map);

        userStatusMap.put("registerUserNum", registerUserNum);
        userStatusMap.put("availUserNum", availUserNum);
        userStatusMap.put("forbiddenUserNum", forbiddenUserNum);
        userStatusMap.put("registerByInvite", registerByInvite);
        userStatusMap.put("registerByport", registerByport);
        userStatusMap.put("registerByImport", registerByImport);
        return userStatusMap;
    }

    public Map<String, Object> tryDeserialize(String jsonString) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            result = (Map<String, Object>) JsonUtils.getInstance().deserialize(jsonString);
        } catch (Exception e) {
            LOG.error("deserialize_json_error", e);
        }
        return result;

    }

    */
/***
     * 初始化sitemap对象的时候同时将菜单保存到对应的子系统里面
     * @param authConfig
     * @param siteMap
     *//*

    public void initClientModule(AuthConfig authConfig, SiteMap siteMap) {
        Map<String, ISiteEntry> entryMap = siteMap.getDefaultSiteMap().getEntryMap();
        List<SubSystemList> subSystems = authConfig.getSubSystems();
        DaoHandler daoHandler = DaoHandler.getInstance();
        SoaSysRole adminRole = (SoaSysRole) daoHandler.getEntity("SoaSysRole", SoaConstants.SOA_SYS_ROLE_SID_ADMIN);
        SoaOauthClient client = (SoaOauthClient) daoHandler.getEntity("SoaOauthClient", SoaConstants.OAUTH_CLIENT_ID_SOA);
        for (SubSystemList sub : subSystems) {
            Set<ModuleList> moduleLists = sub.getModuleLists();
            List<ModuleList> orderedList = new WrapCollection(moduleLists).sortByFld("modNo", true).toList();
            for (ModuleList moduleList : orderedList) {
                SoaOauthModule module = new SoaOauthModule();
                module.setClient(client);
                String appEntry = moduleList.getAppEntry();
                ISiteEntry entry = entryMap.get(appEntry);
                module.setUniqueCode(moduleList.getAppEntry());
                module.setName(entry.getName());
                module.setModuleGroup(entry.getParentEntry().getName());
                module.setModuleUrl(formatMainUrl(entry.getMainUrl()));
                daoHandler.saveEntity(module);
                SoaSysRoleModuleMapping roleMapping = new SoaSysRoleModuleMapping();
                roleMapping.setSoaOauthModule(module);
                roleMapping.setSoaSysRole(adminRole);
                daoHandler.saveEntity(roleMapping);
            }
        }
    }

    private String formatMainUrl(String url) {
        return url.replaceAll("\\[contextPath\\]", "").replaceAll("amp;", "");
    }

    public Long random(int start, int end) {
        double randomValue = (end - start) * Math.random() + start;
        return Variant.valueOf(randomValue).longValue();
    }
}*/
