package com.jd.workflow.console.controller.doc;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.jsf.open.api.vo.InterfaceInfo;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.Authorization;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.ParseType;
import com.jd.workflow.console.dto.doc.DocReportDto;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.parser.InterfaceInfoDown;
import com.jd.workflow.console.helper.UserPrivilegeHelper;
import com.jd.workflow.console.model.sync.InterfaceJsonInfo;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.doc.IDocReportService;
import com.jd.workflow.console.utils.DownloadUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author wufagang
 * @description
 * @date 2023年04月20日 20:43
 */
@Slf4j
@RestController
@RequestMapping("/docByMan")
@UmpMonitor
@Api(tags="文档上报")
public class DocReportByManController {
    @Autowired
    private IDocReportService reportService;

    @Autowired
    private IInterfaceManageService iInterfaceManageService;

    /**
     * 上报接口文档
     * @param dto
     * @return com.jd.workflow.console.base.CommonResult<java.lang.Boolean>
     */
    @PostMapping(value = "/reportDoc")
    @ResponseBody
    @Authorization(key = "interfaceId", parseType = ParseType.BODY)
    public CommonResult<Boolean> reportDoc(@RequestBody DocReportDto dto) {

        reportService.reportDocHashPrivilege(dto);
        return CommonResult.buildSuccessResult(true);
    }
    /**
     * 根据文件解析数据列表
     * @author wufagang
     * @date 2023/4/17 09:48
     * @param file
     * @param response
     */
    @PostMapping(value = "/parseFile")
    public CommonResult<Map<String, List<MethodManage>>> parseFile(@RequestParam("file") MultipartFile file, HttpServletResponse response) {
        return CommonResult.buildSuccessResult(reportService.parseFile(file));
    }

    /**
     * 根据url解析数据列表
     * @author wufagang
     * @date 2023/4/17 09:48
     * @param url
     */
    @GetMapping(value = "/parseUrl")
    public CommonResult<Map<String, List<MethodManage>>> parseUrl(@RequestParam("url") String url) {
        return CommonResult.buildSuccessResult(reportService.parseUrl(url));
    }

    /**
     * 根据应用id模糊检索项目列表
     * @author wufagang
     * @date 2023/4/17 09:48
     * @param appCode 应用code
     * @param search 项目code 支持模糊检索
     * @param current 当前页
     * @param size 页size
     * @param autoReport 自动上报 默认查询非自动上报接口
     */
    @GetMapping(value = "/findInterfaceList")
    public CommonResult<Page<InterfaceManage>> findInterfaceList(@RequestParam("appCode") String appCode,
                                                                 @RequestParam("search") String search,
                                                                 @RequestParam(value = "includeAuto",required = false,defaultValue = "0") Integer autoReport,
                                                                 @RequestParam(value = "current", required = false) Long current,
                                                                 @RequestParam(value = "size",required = false) Long size) {
        return CommonResult.buildSuccessResult(iInterfaceManageService.findInterfaceList(appCode,search,current,size,autoReport));
    }


    /**
     * 从JSF平台同步接口文档
     * @param interfaceJsonInfo
     * @return
     */
    @PostMapping(value = "/syncJsfDoc")
    @ResponseBody
    public void syncDocFromJsfPlatform(@RequestBody InterfaceJsonInfo interfaceJsonInfo,HttpServletResponse response){
        reportService.syncDocFromJsfPlatform(interfaceJsonInfo);
        int i = 0;
    }
}
