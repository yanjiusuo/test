package com.jd.workflow.console.controller;


import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.jingme.ButtonDTO;
import com.jd.workflow.console.dto.jingme.CustomDTO;
import com.jd.workflow.console.dto.jingme.TemplateMsgDTO;
import com.jd.workflow.console.dto.jingme.UserDTO;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.jingme.SendMsgService;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.console.dto.TestDTO;
import com.jd.workflow.console.dto.UserInfoDTO;
import com.jd.workflow.console.service.UserService;

import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.JsonTypeUtils;
import io.swagger.annotations.Api;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@RestController
@Slf4j
@RequestMapping("/demo")
@UmpMonitor
@Api(hidden = true)
public class HelloController {

    @Autowired
    UserService userService;

    @Resource
    IMethodManageService methodManageService;

    @Autowired
    private SendMsgService sendMsgService;

//    @Autowired
//    JssUtil jssUtil;

    @PostConstruct
    public void init() {
        //Route route = destContext.getRoutes().get(0);
        //consumer = route.getConsumer();

    }


    @GetMapping(value = "/index")
    @ResponseBody
    public String index() {

        TemplateMsgDTO templateMsgDTO = new TemplateMsgDTO();
        templateMsgDTO.setHead("接口文档变更通知");
        templateMsgDTO.setSubHeading("上报了接口");
        UserDTO userDTO = new UserDTO();
        userDTO.setErp("zhangqian346");
        userDTO.setRealName("唐倩倩");
        userDTO.setTenantCode("ee");
        List<UserDTO> userDTOList = Lists.newArrayList();
        userDTOList.add(userDTO);
        templateMsgDTO.setAtUsers(userDTOList);

        List<CustomDTO> customDTOList = Lists.newArrayList();
        CustomDTO customDTO = new CustomDTO();
        customDTO.setName("应用名称");
        customDTO.setDescription("联调平台");
        customDTOList.add(customDTO);

        templateMsgDTO.setCustomFields(customDTOList);

        List<ButtonDTO> buttonDTOList = Lists.newArrayList();
        ButtonDTO buttonDTO = new ButtonDTO();
        buttonDTO.setName("查看详情");
        buttonDTO.setPcUrl("http://console.paas.jd.com/idt/fe-app-view/demandManage/26");
        buttonDTOList.add(buttonDTO);

        templateMsgDTO.setButtons(buttonDTOList);

        templateMsgDTO.setContent("此次上报共新增XX个http接口，变更XX个http接口；新增XX个jsf接口，变更XX个jsf接口；");

        String response = sendMsgService.sendUserJueMsg("tangqianqian11", templateMsgDTO);

        return response;
    }

    @GetMapping(value = "/del")
    @ResponseBody
    public String del() {
        sendMsgService.delTeamToken();
        return "ok";
    }


}