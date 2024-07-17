package com.jd.workflow.export;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.attach.impl.OutlineHandler;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutline;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;


import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Html2PdfUtils {

    /**
     * html转pdf
     *
     * @param inputStream  输入流
     * @param waterMark    水印
     * @param fontPath     字体路径，ttc后缀的字体需要添加<b>,0<b/>
     * @param outputStream 输出流
     * @date : 2021/1/15 14:07
     */
    public static void convertToPdf(InputStream inputStream, String waterMark, String fontPath, OutputStream outputStream) throws IOException {

        PdfWriter pdfWriter = new PdfWriter(outputStream);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        PdfOutline outline = pdfDocument.getOutlines(false);

        //设置为A4大小
        pdfDocument.setDefaultPageSize(PageSize.A4);
        //添加水印
        //pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, new WatermarkingEventHandler()); // waterMark

        //添加中文字体支持
        ConverterProperties properties = new ConverterProperties();

        OutlineHandler standardHandler = OutlineHandler.createStandardHandler();
        properties.setOutlineHandler(standardHandler);

        FontProvider fontProvider = new FontProvider();

        //        设置字体
        /*PdfFont sysFont = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H", false);
        fontProvider.addFont(sysFont.getFontProgram(), "UniGB-UCS2-H");*/

        //添加自定义字体，例如微软雅黑
        if (StringUtils.isNotBlank(fontPath)) {
            PdfFont microsoft = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H, false);
            fontProvider.addFont(microsoft.getFontProgram(), PdfEncodings.IDENTITY_H);
        }

        properties.setFontProvider(fontProvider);
        //        读取Html文件流，查找出当中的&nbsp;或出现类似的符号空格字符
        inputStream = readInputStrem(inputStream);
        if (inputStream != null) {
            //        生成pdf文档
            HtmlConverter.convertToPdf(inputStream, pdfDocument, properties);
            pdfWriter.close();
            pdfDocument.close();
            return;
        } else {
            log.error("转换失败！");
        }
    }

    // 水印
    protected static class WatermarkingEventHandler implements IEventHandler {

        protected float x = 298f;
        protected float y = 421f;
        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            PdfFont font = null;
            try {
                font = PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD);
            } catch (IOException e) {
                e.printStackTrace();
            }
            PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
            new Canvas(canvas, pdfDoc, page.getPageSize())
                    //.setFontColor(com.itextpdf.kernel.colors.Color.LIGHT_GRAY)
                    .setFontSize(60)
                    .setFont(font)
                    .showTextAligned(new Paragraph("W A T E R M A R K"), x, y, pdfDoc.getPageNumber(page),
                            TextAlignment.CENTER, VerticalAlignment.MIDDLE, 45);
        }
    }
    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }
    /**
     * 读取HTML 流文件，并查询当中的&nbsp;或类似符号直接替换为空格
     *
     * @param inputStream
     * @return
     */
    private static InputStream readInputStrem(InputStream inputStream) {
        // 定义一些特殊字符的正则表达式 如：
        String regEx_special = "\\&[a-zA-Z]{1,10};";
        try {
            //<1>创建字节数组输出流，用来输出读取到的内容
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //<2>创建缓存大小
            byte[] buffer = new byte[1024]; // 1KB
            //每次读取到内容的长度
            int len = -1;
            //<3>开始读取输入流中的内容
            while ((len = inputStream.read(buffer)) != -1) { //当等于-1说明没有数据可以读取了
                baos.write(buffer, 0, len);   //把读取到的内容写到输出流中
            }
            //<4> 把字节数组转换为字符串
            String content = baos.toString();
            //<5>关闭输入流和输出流
            //            inputStream.close();
            baos.close();
            //            log.info("读取的内容：{}", content);
            //            判断HTML内容是否具有HTML的特殊字符标记
            Pattern compile = Pattern.compile(regEx_special, Pattern.CASE_INSENSITIVE);
            Matcher matcher = compile.matcher(content);
            String replaceAll = matcher.replaceAll("");
            //            log.info("替换后的内容：{}", replaceAll);
            //            将字符串转化为输入流返回
            InputStream stringStream = getStringStream(replaceAll);
            //<6>返回结果
            return stringStream;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("错误信息：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 将一个字符串转化为输入流
     * @param sInputString 字符串
     * @return
     */
    public static InputStream getStringStream(String sInputString) {
        if (sInputString != null && !sInputString.trim().equals("")) {
            try {
                ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(sInputString.getBytes());
                return tInputStringStream;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        //       html文件所在相对路径
        String htmlFile = "/F:/temp/html/aaa.html";
        //       pdf文件存储相对路径
        String pdfFile = "/F:/temp/html/aaa.pdf";
        //        自定义水印
        String waterMarkText =  "";
        InputStream inputStream = new FileInputStream(htmlFile);
        OutputStream outputStream = new FileOutputStream(pdfFile);
        //微软雅黑在windows系统里的位置如下，linux系统直接拷贝该文件放在linux目录下即可
        //        String fontPath = "src/main/resources/font/STHeiti Light.ttc,0";
        //String fontPath = "src/main/resources/font/simsun.ttc,0";
        String fontPath = "C:\\Windows\\Fonts/simsun.ttc,0";
        convertToPdf(inputStream, "藏经阁-在线联调", fontPath, outputStream);
        log.info("转换结束，耗时：{}ms",System.currentTimeMillis()-startTime);
    }
}
