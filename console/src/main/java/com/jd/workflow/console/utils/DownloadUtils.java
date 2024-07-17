package com.jd.workflow.console.utils;

import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/5/7
 */
@Slf4j
public class DownloadUtils {
    public static void download(HttpServletResponse response, Class t, List list) {
        try {
            response.setContentType("application/vnd.ms-excel");// 设置文本内省
            response.setCharacterEncoding("utf-8");// 设置字符编码
            response.setHeader("Content-disposition", "attachment;filename=demo.xlsx"); // 设置响应头
            EasyExcel.write(response.getOutputStream(), t).sheet("模板").doWrite(list); //用io流来写入数据
        } catch (IOException e) {
            log.error("JsfTestController.download exception ",e);
        }
    }
}
