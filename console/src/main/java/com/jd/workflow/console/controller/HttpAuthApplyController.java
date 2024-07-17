package com.jd.workflow.console.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.SiteEnum;
import com.jd.workflow.console.controller.excel.EasyExcelListener;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.entity.HttpAuthApplyXbpParam;
import com.jd.workflow.console.service.IHttpAuthApplyService;
import com.jd.workflow.soap.common.lang.Guard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.List;

/**
 * 项目名称：鉴权标识服务
 *
 * @author wangwenguang
 * @date 2023-01-06 11:31
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/httpAuthApply")
@UmpMonitor
public class HttpAuthApplyController {

    /**
     * 鉴权标识服务
     */
    @Resource
    private IHttpAuthApplyService httpAuthApplyService;


    /**
     * 查询已申请鉴权标识列表
     *
     * @param query
     * @return
     */
    @PostMapping("/queryList")
    public CommonResult queryList(@RequestBody QueryHttpAuthApplyReqDTO query) {
        log.info("#HttpAuthApplyController queryList requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "查询已申请鉴权标识列表时，入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(query.getSite()), "查询已申请鉴权标识列表时，site 参数不正确！");

        query.setPin(UserSessionLocal.getUser().getUserId());
        //TODO 核实一下审批状态
//        query.setTicketStatus();
        Page<HttpAuthApplyDTO> httpAuthPage = httpAuthApplyService.queryListPage(query);
        log.info("#HttpAuthApplyController queryList result={} ", JSON.toJSONString(httpAuthPage));

        return CommonResult.buildSuccessResult(httpAuthPage);
    }


    /**
     * 查询已申请鉴权标识列表
     *
     * @param query
     * @return
     */
    @PostMapping("/queryListByTicketId")
    public CommonResult queryListByTicketId(@RequestBody QueryHttpAuthApplyReqDTO query) {
        log.info("#HttpAuthApplyController queryListByTicketId requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "查询已申请鉴权标识列表时，入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(query.getSite()), "查询所有已申请鉴权标识列表时，site 参数不正确！");
        Guard.notNull(query.getTicketId(), "查询已申请鉴权标识列表时，ticketId 参数不能为空！");

        query.setPin(UserSessionLocal.getUser().getUserId());

        List<HttpAuthApplyDTO> authApplyDTOList = httpAuthApplyService.queryAllList(query);
        log.info("#HttpAuthApplyController queryListByTicketId result={} ", JSON.toJSONString(authApplyDTOList));

        return CommonResult.buildSuccessResult(authApplyDTOList);
    }

    /**
     * 查询已申请鉴权标识列表
     *
     * @param applyParamDTO
     * @return
     */
    @PostMapping("/submit")
    public CommonResult submit(@RequestBody HttpAuthApplyParamDTO applyParamDTO) {
        log.info("#HttpAuthApplyController submit requestBody={} ", JSON.toJSONString(applyParamDTO));

        Guard.notNull(applyParamDTO, "申请鉴权标识时，入参不能为空");

        boolean success = httpAuthApplyService.submit(applyParamDTO);
        log.info("#HttpAuthApplyController submit result={} ", success);

        return CommonResult.buildSuccessResult(success);
    }



    /**
     * execl模板导入数据
     */
    @ResponseBody
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public CommonResult upload(@RequestParam(value = "file", required = true) CommonsMultipartFile file,
                               @RequestParam(value = "appCode", required = false) String appCode,
                               @RequestParam(value = "appName", required = false) String appName) {
        CommonResult commonResult = null;
        int i = 1;
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            EasyExcelListener authApplyListener =  new EasyExcelListener();
            ExcelReader excelReader = EasyExcel.read(inputStream).build();
            ReadSheet readSheet1 = EasyExcel.readSheet(0).headRowNumber(3).head(HttpAuthApplyDTO.class).registerReadListener(authApplyListener).build();
            excelReader.read(readSheet1);
            excelReader.finish();
            List<HttpAuthApplyDTO> authApplyList = authApplyListener.getData();
            log.error("excel导入数据authApplyList={} ,authApplyDetailList={} ", JSON.toJSONString(authApplyList));

            HttpAuthApplyResultDTO applyResultDTO = httpAuthApplyService.importApplyData(appCode,appName,authApplyList);
            //读取表格第一个sheet
            commonResult = CommonResult.buildSuccessResult(applyResultDTO);
        } catch (Exception e) {
            e.printStackTrace();
            String message = "excel导入数据错误，请稍后再试！";
            if (e.getMessage().equals("Strict OOXML")){
                message = "excel导入数据错误，上传文件不能是Strict Open XML 格式！";
            }
            commonResult = CommonResult.buildErrorCodeMsg(1, message);
            log.error("excel导入数据错误！，{}", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                log.error("#upload inputStream.close() error ={}", e);
            }
        }
        return commonResult;
    }

    @RequestMapping("/pushXbpDta")
    @ResponseBody
    public CommonResult<Boolean> pushXbpData(@RequestBody HttpAuthApplyXbpParam param){
        httpAuthApplyService.callBackXbpFlow(param);
        return CommonResult.buildSuccessResult(true);
    }

}