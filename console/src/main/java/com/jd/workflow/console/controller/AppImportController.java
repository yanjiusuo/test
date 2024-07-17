package com.jd.workflow.console.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.controller.excel.EasyExcelListener;
import com.jd.workflow.console.dto.AppImportDTO;
import com.jd.workflow.console.dto.AppImportResultDTO;
import com.jd.workflow.console.service.AppImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 导入藏经阁应用
 * @author xiaobei
 * @date 2023-02-24 21:09
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/app")
@UmpMonitor
public class AppImportController {

    private AppImportService appImportService;

    @Autowired
    public void setAppImportService(AppImportService appImportService) {
        this.appImportService = appImportService;
    }

    /**
     * execl模板导入数据
     */
    @ResponseBody
    @RequestMapping(value = "import/upload", method = RequestMethod.POST)
    public CommonResult<AppImportResultDTO> upload(@RequestParam(value = "file", required = true) CommonsMultipartFile file) {
        CommonResult<AppImportResultDTO> commonResult = null;
        int i = 1;
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            EasyExcelListener<AppImportDTO> authApplyListener =  new EasyExcelListener<>();
            ExcelReader excelReader = EasyExcel.read(inputStream).build();
            ReadSheet readSheet = EasyExcel.readSheet(0).headRowNumber(3)
                    .head(AppImportDTO.class).registerReadListener(authApplyListener).build();
            excelReader.read(readSheet);
            excelReader.finish();
            // 获取解析后的数据
            List<AppImportDTO> authApplyList = authApplyListener.getData();
            log.error("excel导入应用数据authApplyList={}", JSON.toJSONString(authApplyList));
            String erp = UserSessionLocal.getUser().getUserId();
            // 调用jsf接口进行应用数据上传并返回上传结果
            AppImportResultDTO result = appImportService.batchImportCjgApp(authApplyList, erp);
            //读取表格第一个sheet
            commonResult = CommonResult.buildSuccessResult(result);
        } catch (Exception e) {
            e.printStackTrace();
            commonResult = CommonResult.buildErrorCodeMsg(1, "excel导入数据错误！excel行数: " + String.valueOf(i) + e.getMessage());
            log.error("excel导入数据错误！", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                log.error("#upload inputStream.close() error is ", e);
            }
        }
        return commonResult;
    }
}
