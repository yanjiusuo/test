package com.jd.workflow.console.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.LoginTypeEnum;
import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.dto.UserInfoDTO;
import com.jd.workflow.console.dto.UserPinDTO;
import com.jd.workflow.console.dto.dept.QueryDeptResultDTO;
import com.jd.workflow.console.entity.UserInfo;
import com.jd.workflow.console.service.IUserInfoService;
import com.jd.workflow.console.service.plugin.PluginLoginService;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.BeanTool;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.swagger.annotations.ApiOperation;

/**
 * <p>
 * 用户信息表 前端控制器
 * </p>
 *
 * @author wubaizhao1
 * @since 2022-05-11
 */
@Slf4j
@RestController
@RequestMapping("/userInfo")
@UmpMonitor
@Api(value = "用户管理",tags="用户管理")
public class UserInfoController {

    /**
     * @date: 2022/5/13 10:51
     * @author wubaizhao1
     */
    @Resource
    IUserInfoService userInfoService;

    @Autowired
    PluginLoginService pluginLoginService;

    /**
     * 新增
     * 出参: id
     *
     * @param userInfoDTO
     * @return
     * @date: 2022/5/11 19:54
     * @author wubaizhao1
     */
    @PostMapping("/addUser")
    @ApiOperation(value = "新增用户")
    public CommonResult addUser(@RequestBody UserInfoDTO userInfoDTO) {
        log.info("UserInfoController add query={}", JsonUtils.toJSONString(userInfoDTO));
        //1.判空
        Guard.notEmpty(userInfoDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        //2.入参封装
        //		String tenantId = UserSessionLocal.getUser().getTenantId();
        String operator = UserSessionLocal.getUser().getUserId();
        //		String userName = UserSessionLocal.getUser().getUserName();
        //		userInfoDTO.setUserName(userName);
        //		userInfoDTO.setUserCode(operator);
        //3.service层
        Long id = userInfoService.add(userInfoDTO);
        //4.出参
        return CommonResult.buildSuccessResult(id);
    }

    @PostMapping("/registerUser")
    public CommonResult registerUser(@RequestBody UserInfoDTO userInfoDTO) {
        log.info("UserInfoController add query={}", JsonUtils.toJSONString(userInfoDTO));
        //1.判空
        Guard.notEmpty(userInfoDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        //2.入参封装
        //		String tenantId = UserSessionLocal.getUser().getTenantId();
        String operator = UserSessionLocal.getUser().getUserId();
        //		String userName = UserSessionLocal.getUser().getUserName();
        //		userInfoDTO.setUserName(userName);
        //		userInfoDTO.setUserCode(operator);
        //3.service层
        Long id = userInfoService.register(userInfoDTO);
        //4.出参
        return CommonResult.buildSuccessResult(id);
    }

    /**
     * 登陆后自动校验新增用户
     * 出参: id
     *
     * @param userInfoDTO
     * @return
     * @date: 2022/5/11 19:54
     * @author wubaizhao1
     */
    @PostMapping("/autoCheckAndAddUser")
    @ApiOperation(value = "登陆后自动校验新增用户")
    public CommonResult autoCheckAndAddUser(@RequestBody UserInfoDTO userInfoDTO) {
        log.info("UserInfoController add query={}", JsonUtils.toJSONString(userInfoDTO));
        //1.判空
        Guard.notEmpty(userInfoDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        //2.入参封装
        String operator = UserSessionLocal.getUser().getUserId();
        String userName = UserSessionLocal.getUser().getUserName();
        userInfoDTO.setUserName(userName);
        userInfoDTO.setUserCode(operator);
        if (userInfoDTO.getLoginType() == null) {
            userInfoDTO.setLoginType(LoginTypeEnum.DEFAULT.getCode());
        }
        //3.service层
        UserInfo one = userInfoService.getOne(operator);
        Long id = null;
        if (one == null) {
            id = userInfoService.add(userInfoDTO);
        }
        //4.出参
        return CommonResult.buildSuccessResult(id);
    }

    /**
     * 入参： 唯一索引 id  或者 用户编码
     * 出参： id
     *
     * @param userInfoDTO
     * @return
     * @date: 2022/5/11 19:54
     * @author wubaizhao1
     */
    @PostMapping("/editUser")
    @ApiOperation(value = "修改用户")
    public CommonResult editUser(@RequestBody UserInfoDTO userInfoDTO) {
        log.info("UserInfoController edit query={}", JsonUtils.toJSONString(userInfoDTO));
        //1.判空
        //2.入参封装
        String operator = UserSessionLocal.getUser().getUserId();
        //		String userName = UserSessionLocal.getUser().getUserName();
        //		userInfoDTO.setUserName(userName);
        //		userInfoDTO.setUserCode(operator);
        //3.service层
        Long id = userInfoService.edit(userInfoDTO);
        //4.出参
        return CommonResult.buildSuccessResult(id);
    }

    /**
     * 根据 usercode或者id删除用户
     *
     * @param userInfoDTO
     * @return
     */
    @PostMapping("/removeUser")
    @ApiOperation(hidden = true,value = "删除用户remove")
    public CommonResult removeUser(@RequestBody UserInfoDTO userInfoDTO) {
        //1.判空
        //2.入参封装
        String operator = UserSessionLocal.getUser().getUserId();
        log.info("UserInfoController removeUser query={},operator={}", JsonUtils.toJSONString(userInfoDTO), operator);
        //3.service层
        Boolean ref = userInfoService.remove(userInfoDTO);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * 获取用户信息 根据usercode
     *
     * @param userInfoDTO
     * @return
     * @date: 2022/5/12 14:04
     * @author wubaizhao1
     */
    @GetMapping("/getOneUser")
    @ApiOperation(value = "getOne")
    public CommonResult getOneUser(UserInfoDTO userInfoDTO) {
        log.info("UserInfoController getOne query={}", JsonUtils.toJSONString(userInfoDTO));
        //1.判空
        //2.入参封装
        //		String operator="system";
        //3.service层
        UserInfo ref = userInfoService.getOne(userInfoDTO.getUserCode());
        //4.出参
        return CommonResult.buildSuccessResult(ref.toMap());
    }

    /**
     * 根据erp或者其他字段进行模糊搜索
     * 入参: 登录类型,用户编码(erp手机号等)
     * 出参: List<UserInfo>
     *
     * @param userInfoDTO
     * @return
     * @date: 2022/5/12 18:20
     * @author wubaizhao1
     */
    @GetMapping("/listUserByCode")
    @ApiOperation(value = "List模糊搜索")
    public CommonResult<List<UserInfo>> listByCode(UserInfoDTO userInfoDTO) {
        log.info("UserInfoController listByCode query={}", JsonUtils.toJSONString(userInfoDTO));
        //1.判空
        //2.入参封装
        //		String operator="system";
        //3.service层
        List<UserInfo> userInfos = userInfoService.listByCode(userInfoDTO);
        //4.出参
        return CommonResult.buildSuccessResult(userInfos);
    }

    @GetMapping("/getLoginOne")
    @ApiOperation(value = "获取登录者的基本信息")
    public CommonResult getLoginOne() {
        log.info("UserInfoController getLoginOne");
        //1.判空
        //3.service层
        UserInfo userInfo = userInfoService.getLoginOne();
        //4.出参
        return CommonResult.buildSuccessResult(userInfo.toMap());
    }

    /**
     * @return
     * @date: 2022/6/14 14:39
     * @author wubaizhao1
     */
    @PostMapping("/addPin")
    public CommonResult addPin(@RequestBody UserPinDTO userPinDTO) {
        log.info("UserInfoController addPin");
        //1.判空
        //3.service层
        Boolean ref = userInfoService.addPin(userPinDTO);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * 编辑
     *
     * @param userPinDTO
     * @return
     * @date: 2022/6/15 16:27
     * @author wubaizhao1
     */
    @PostMapping("/editPin")
    public CommonResult editPin(@RequestBody UserPinDTO userPinDTO) {
        log.info("UserInfoController editPin");
        //1.判空
        //3.service层
        Boolean ref = userInfoService.editPin(userPinDTO);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * 检查是否是pin用户的成员
     *
     * @param userCode
     * @return
     */
    @GetMapping("/getPin")
    public CommonResult getPin(String userCode) {
        log.info("UserInfoController getPin");
        //1.判空
        //3.service层
        Boolean ref = userInfoService.getPin(userCode);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * 管理列表
     *
     * @param userPinDTO
     * @return
     * @date: 2022/6/15 16:28
     * @author wubaizhao1
     */
    @GetMapping("/pageListUserPinDTO")
    public CommonResult pageListUserPinDTO(UserPinDTO userPinDTO) {
        log.info("UserInfoController getPin");
        //1.判空
        //3.service层
        Page<UserPinDTO> ref = userInfoService.pageListUserPinDTO(userPinDTO);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * 获取部门树
     * @author wufagang
     * @date 2023/4/18 10:56
     * @param parentOrganizationCode 
     * @return com.jd.workflow.console.base.CommonResult<com.jd.workflow.console.dto.dept.QueryDeptResultDTO>
     */
    @GetMapping("/getAllDept")
    public CommonResult<List<QueryDeptResultDTO>>  getAllDept(@ApiParam(value = "父部门编码", type = "string")
                                  @RequestParam(value = "parentOrganizationCode", required = false) String parentOrganizationCode) {
        return CommonResult.buildSuccessResult(userInfoService.getAllDept(parentOrganizationCode));
    }

    /**
     * 获取用户基本信息
     * @param erp 用户erp
     * @return 用户基本信息，headImg为用户头像
     */
    @GetMapping("/getUserBaseInfo")
    public CommonResult<UserInfo> getUserBaseInfo(String erp){
        Guard.notEmpty(erp,"用户erp不可为空");

        UserInfo userInfo = userInfoService.getUser(erp);

        return CommonResult.buildSuccessResult(userInfo);
    }

    /**
     * 获取当前登录用户的userToken
     * @return 登录后的token
     */
    @GetMapping("/getUserToken")
    public CommonResult<String> getUserToken(){
        String userId = UserSessionLocal.getUser().getUserId();
        String encrypt = pluginLoginService.encrypt(userId);


        return CommonResult.buildSuccessResult(encrypt);
    }

}
