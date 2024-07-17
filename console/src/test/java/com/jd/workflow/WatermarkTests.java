package com.jd.workflow;

import com.jd.security.watermark.docwatermarksdk.config.DocWatermarkSdkConfig;
import com.jd.security.watermark.docwatermarksdk.entity.TextTheme;
import com.jd.security.watermark.docwatermarksdk.service.WatermarkPdfService;
import com.jd.security.watermark.docwatermarksdk.service.impl.WatermarkPdfServiceImpl;
import org.springframework.util.ResourceUtils;

import java.awt.*;
import java.io.*;

public class WatermarkTests {
    /*
    watermark.config.appCode=data-flow
watermark.config.token=91ex9Xdo5j4Y2RDdtBqaNF
watermark.config.url=http://watermark-internal.jd.com/api/data/watermark
watermark.config.version=1.1.0
     */
    public static void main(String[] args) throws IOException, FontFormatException {
        DocWatermarkSdkConfig sdkConfig = new DocWatermarkSdkConfig();
        sdkConfig.setUrl("http://watermark-internal.jd.com/api/data/watermark");
        sdkConfig.setAppCode("data-flow");
        sdkConfig.setToken("91ex9Xdo5j4Y2RDdtBqaNF");
        WatermarkPdfService service = new WatermarkPdfServiceImpl(sdkConfig);
        File fontFile = ResourceUtils.getFile("classpath:font/simsun.ttc");

        Font font = Font.createFont(Font.PLAIN, new FileInputStream(fontFile) ).deriveFont(30f);
        FileInputStream inputStream = new FileInputStream("F:\\temp\\html\\aaa.pdf");
        FileOutputStream outputStream = new FileOutputStream("F:\\temp\\html\\bbb.pdf");
        TextTheme textTheme = TextTheme.builder()
                .font(font)
                //.font(new Font("SansSerif", Font.PLAIN, 12))
                .fontColor(new Color(0xbfbfbf))
                .rotation(-25.0)
                .density(1.0) // 密度
                .textOpacity(0.1f)
                .isBranch(true)
                .additionalText("藏经阁一体化")
                .build();
        service.addWatermark(inputStream,outputStream,
                "wangjingfang3", textTheme
                );
    }
}
