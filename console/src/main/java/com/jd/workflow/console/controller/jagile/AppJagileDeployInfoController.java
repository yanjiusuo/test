package com.jd.workflow.console.controller.jagile;


import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.service.doc.SyncDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 行云jdos部署记录表 前端控制器
 * </p>
 *
 * @author zhaojingchun
 * @since 2023-07-10
 */
@RestController
@RequestMapping("/deployInfo")
public class AppJagileDeployInfoController {
    @Autowired
    private SyncDocService syncDocService;

    @GetMapping(path = "test")
    public CommonResult<String> pageList() {
        syncDocService.syncDocDependDeployInfo();
        return CommonResult.buildSuccessResult("成功");
    }
}
