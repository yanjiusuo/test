package com.jd.workflow.console.controller;

import com.jd.workflow.console.base.annotation.Authorization;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.AuthorizationKeyTypeEnum;
import com.jd.workflow.console.base.enums.ParseType;
import com.jd.workflow.console.entity.MemberRelation;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dto.MemberRelationDTO;
import com.jd.workflow.console.service.IMemberRelationService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import io.swagger.annotations.ApiOperation;

/**
 * 接口成员关联表 前端控制器
 *
 * @author wubaizhao1
 * @since 2022-05-11
 */
@Slf4j
@RestController
@RequestMapping("/memberRelation")
@UmpMonitor
@Api(value = "接口成员关联",tags="接口成员关联")
public class MemberRelationController {

    @Resource
    IMemberRelationService memberRelationService;

    @PostMapping("/binding")
    public CommonResult<Boolean> binding(@RequestBody MemberRelationDTO memberRelationDTO) {
        log.info("MemberRelationController binding query={}", JsonUtils.toJSONString(memberRelationDTO));
        //1.判空
        //2.入参封装
        String operator = UserSessionLocal.getUser().getUserId();
        //3.service层
        Boolean result = memberRelationService.binding(memberRelationDTO);
        //4.出参
        return CommonResult.buildSuccessResult(result);
    }
    @PostMapping("/removeRelation")
    public CommonResult<Boolean> removeRelation(Long id) {
        //MemberRelation relation = memberRelationService.getById(id);
        boolean result = memberRelationService.removeById(id);
        //4.出参
        return CommonResult.buildSuccessResult(result);
    }
    @PostMapping("/unBinding")
    @Authorization(key = "id", keyType = AuthorizationKeyTypeEnum.RELATION, parseType = ParseType.BODY)
    @ApiOperation(value = "成员管理 删除成员")
    public CommonResult<Boolean> unBinding(@RequestBody MemberRelationDTO memberRelationDTO) {
        log.info("MemberRelationController unBinding query={}", JsonUtils.toJSONString(memberRelationDTO));
        //1.判空
        //2.入参封装
        String operator = UserSessionLocal.getUser().getUserId();
        //3.service层
        Boolean result = memberRelationService.unBinding(memberRelationDTO);
        //4.出参
        return CommonResult.buildSuccessResult(result);
    }
}
