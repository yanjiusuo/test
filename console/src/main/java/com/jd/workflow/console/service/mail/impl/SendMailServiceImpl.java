package com.jd.workflow.console.service.mail.impl;

import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.service.mail.SendMailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import java.io.File;
import java.util.Map;

/**
 * 发送邮件服务
 * @author xiaobei
 * @date 2022-03-10 15:35
 */
@Slf4j
public class SendMailServiceImpl implements SendMailService {

    private JavaMailSender mailSender;

    private String from;
    private String to;

    private SpringTemplateEngine springTemplateEngine;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setSpringTemplateEngine(SpringTemplateEngine springTemplateEngine) {
        this.springTemplateEngine = springTemplateEngine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send( String[] to, String subject, String fileUrl, File file, String fileName) {
        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            String toEmail = getTo();
            if(StringUtils.isNotBlank(toEmail)){
                helper.setTo(toEmail.split(","));
            }else {
                helper.setTo(to);
            }
            helper.setFrom(from);
            helper.setSubject(subject);
            // 比如上传一张图片作为附件
            FileSystemResource fileResource = new FileSystemResource(file);
            //附件名称
            helper.addAttachment(fileName, fileResource);
            // 再比如添加一个内联资源，注意 setText 方法要放在 addInline 前面
            // 第二个参数指定为true可以发送html文本
            String text = "<html><body>本次jdos应用批量关联藏经阁应用已完成，您可通过附件直接下载，也可点击【下载】链接，" +
                    "下载导入结果明细数据：<a href=\"" + fileUrl + "\">"+ fileName +"</a></body></html>";
            helper.setText(text, true);
        };
        try {
            this.mailSender.send(preparator);
            log.info("发送成功...");
        } catch (MailException e) {
            e.printStackTrace();
            log.error("邮件发送失败", e);
            throw new RuntimeException("邮件发送失败" + e.getMessage());
        }
    }

    @Override
    public void send(String[] to, String title, String text) {
        log.info("#SendMailServiceImpl.send.to={},title={}", to,title);
        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(to);
            helper.setFrom(from);
            helper.setSubject(title);
            helper.setText(text, true);
        };
        try {
            log.info("#SendMailServiceImpl.send.request= {}", JSON.toJSONString(preparator));
            this.mailSender.send(preparator);
            log.info("#SendMailServiceImpl.send.result 邮件发送成功...");
        } catch (MailException e) {
            e.printStackTrace();
            log.error("邮件发送失败", e);
            throw new RuntimeException("邮件发送失败" + e.getMessage());
        }
    }

    @Override
    public void send(String[] to, String[] cc, String title, String text) {
        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            String toEmail = getTo();
            if(StringUtils.isNotBlank(toEmail)){
                helper.setTo(toEmail.split(","));
            }else {
                helper.setTo(to);
            }
            helper.setCc(cc);
            helper.setFrom(from);
            helper.setSubject(title);
            helper.setText(text, true);
        };
        try {
            this.mailSender.send(preparator);
            log.info("发送成功...");
        } catch (MailException e) {
            e.printStackTrace();
            log.error("邮件发送失败", e);
            throw new RuntimeException("邮件发送失败" + e.getMessage());
        }
    }

    @Override
    public String getHtmlText(String template, Map<String, Object> dataMap) {
        Context context = new Context();
        context.setVariables(dataMap);
        return springTemplateEngine.process(template, context);
    }
}
