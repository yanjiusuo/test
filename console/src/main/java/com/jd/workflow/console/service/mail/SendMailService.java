package com.jd.workflow.console.service.mail;

import java.io.File;
import java.util.Map;

/**
 * TODO
 *
 * @author xiaobei
 * @date 2022-03-10 15:34
 */
public interface SendMailService {

    /**
     *
     * @param to
     * @param subject
     * @param fileUrl
     * @param file
     * @param fileName
     */
    void send(String[] to, String subject, String fileUrl, File file, String fileName);

    /**
     *
     * @param to 收件人
     * @param title 邮件标题
     * @param text 邮件内容
     */
    void send(String[] to, String title, String text);

    /**
     *
     * @param to
     * @param cc 抄送
     * @param title
     * @param text
     */
    void send(String[] to, String[] cc, String title, String text);

    /**
     * 根据模板及数据获取解析后的html文本
     * @param template 模板位置
     * @param dataMap 模板中需要的数据
     * @return 解析后的html文本
     */
    String getHtmlText(String template, Map<String, Object> dataMap);
}
