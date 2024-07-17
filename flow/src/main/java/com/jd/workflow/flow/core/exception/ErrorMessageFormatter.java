package com.jd.workflow.flow.core.exception;

import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.util.XmlUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import sun.net.www.protocol.file.FileURLConnection;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ErrorMessageFormatter {
    static final Logger logger = LoggerFactory.getLogger(ErrorMessageFormatter.class);
    static String TEMPLATE_PATH = "flow-error";
    static Map<String,String> errors = new HashMap<>();
    static {
        init();
    }
    private static void loadFromFile(File file,List<String> content) throws IOException {
        if(!file.exists()){
            return;
        }
        if(file.isDirectory()){
            for (File listFile : file.listFiles()) {
                loadFromFile(listFile,content);
            }
        }
        if(!file.getName().endsWith(".xml")) return;

        String result = IOUtils.toString(new FileInputStream(file), "utf-8");
        logger.info("file.load_xml_content:url={},xml={}",file.getAbsolutePath(),result);
        content.add(result);
    }
    private static void loadFromJar(String path,List<String> result,JarURLConnection jarCon) throws IOException {

        JarFile jarFile = jarCon.getJarFile();
        Enumeration<JarEntry> jarEntrys = jarFile.entries();
        //循环jar中所有文件夹
        while (jarEntrys.hasMoreElements()) {
            JarEntry entry = jarEntrys.nextElement();
            String className = entry.getName();

            if (className.startsWith(path) && !entry.isDirectory()
                    && className.endsWith(".xml")
            ) { //判断已路径开头的文件夹及子文件
                String fileName = className.replace(path + "/", "");
                try(InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(className)){
                    String xml = IOUtils.toString(inputStream,"utf-8");
                    logger.info("file.load_jar_xml_content:url={},xml={}",className,xml);
                    result.add(xml);
                };

            }

        }
    }
    public static List<String> loadXmlFile(String path) throws IOException, URISyntaxException {
        List<String > result = new ArrayList<>();
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(path);
        while (urls.hasMoreElements()){
            URL jarURL = urls.nextElement();
            logger.info("msg.load_content_from_url:url={}",jarURL);
            URLConnection urlConnection = jarURL.openConnection();
            if(urlConnection instanceof FileURLConnection){
                FileURLConnection connection = (FileURLConnection)urlConnection;
                connection.connect();
                File file = new File(connection.getURL().toURI());
                loadFromFile(file,result);
            }else{
                loadFromJar(path,result, (JarURLConnection) urlConnection);
            }

        }
        return result;

    }
    private static   void init(){
        try {
            List<String> xmls = loadXmlFile(TEMPLATE_PATH);
            for (String xml : xmls) {
                loadConfig(xml);
            }

            logger.info("file.load_msg_is:content={}",errors);
        } catch (Exception e) {
            logger.error("file.err_load_file_msg",e);
            throw  StdException.adapt(e);
        }
    }
    private static void loadConfig(File file) throws IOException {
        String content = IOUtils.toString(new FileInputStream(file), "utf-8");
        try{
            loadConfig(content);
        }catch (Exception e){
            logger.error("file.err_load_config:file={}",file.getAbsolutePath(),e);
        }
    }
    private static void loadConfig(String content) throws IOException {

        Document document = XmlUtils.parseXml(content);
        Element documentElement = document.getDocumentElement();
        int length = documentElement.getChildNodes().getLength();
        for (int i = 0; i < length; i++) {
            Node node = documentElement.getChildNodes().item(i);
            if(!(node instanceof Element)){
                continue;
            }
            Element error = (Element) node;
            errors.put(error.getAttribute("id"),error.getTextContent().trim());
        }
    }
    static void getAllChildFile(File file,List<File> childFiles){
        if(!file.isDirectory()){
            childFiles.add(file);
            return;
        }
        for (File listFile : file.listFiles()) {
            getAllChildFile(listFile,childFiles);
        }
    }
    public static  String formatMsg(StdException exception){
        if(exception == null) return null;
        if(exception instanceof StepExecException) return formatMsg((StepExecException)exception);
        if(exception instanceof StepParseException) return formatMsg((StepParseException)exception);
        String result = errors.get(exception.getMsg());
        if(result == null){
            return exception.getMessage();
        }
        return StringHelper.replacePlaceholder(result,exception.getParams());
    }
    public static  String formatMsg(StepExecException exception){
        if(exception == null) return null;
        String result = errors.get(exception.getMsg());
        if(result == null){
            return exception.getOriginalMessage();
        }
        String prefix = "步骤"+exception.getStepId()+"执行失败:";
        return prefix+StringHelper.replacePlaceholder(result,exception.getParams());
    }
    public static  String formatMsg(StepParseException exception){
        if(exception == null) return null;
        String result = errors.get(exception.getMsg());
        if(result == null){
            return exception.getMessage();
        }
        String prefix = "步骤"+exception.getId()+"解析失败:";
        return prefix+StringHelper.replacePlaceholder(result,exception.getParams());
    }

    public static void main(String[] args) {

    }
}
