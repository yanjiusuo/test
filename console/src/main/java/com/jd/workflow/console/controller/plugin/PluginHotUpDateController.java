package com.jd.workflow.console.controller.plugin;


import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.service.ducc.entity.HotUpdateEnvironmentConf;
import com.jd.workflow.console.service.plugin.HotUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * idea插件更新
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/hotUpdate")
@UmpMonitor
public class PluginHotUpDateController {
     @Autowired
     HotUpdateService hotUpdateService;
    @RequestMapping("getEnvironmentInfo")
    @ResponseBody
    public List<HotUpdateEnvironmentConf> getEnvironmentInfo() {
        return hotUpdateService.getEnvironmentList();
    }


}
