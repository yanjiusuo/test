package com.jd.workflow.console.controller.local;


import cn.hutool.core.collection.CollectionUtil;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.CaseSourceEnum;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.ResourceRoleEnum;
import com.jd.workflow.console.controller.utils.DtBeanUtils;
import com.jd.workflow.console.dto.group.RequirementTypeEnum;
import com.jd.workflow.console.entity.MemberRelation;
import com.jd.workflow.console.entity.local.LocalRequirementParam;
import com.jd.workflow.console.entity.local.LocalTestRecord;
import com.jd.workflow.console.entity.local.LocalTestRecordParam;
import com.jd.workflow.console.entity.requirement.RequirementInfo;
import com.jd.workflow.console.service.IMemberRelationService;
import com.jd.workflow.console.service.local.ILocalTestRecordService;
import com.jd.workflow.console.service.local.IRRequirementCaseService;
import com.jd.workflow.console.service.requirement.RequirementInfoService;
import com.jd.workflow.soap.common.lang.Guard;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

import static com.jd.workflow.console.entity.requirement.RequirementInfo.STATUS_FINISHED;

/**
 * <p>
 * 本地测试记录流水 前端控制器
 * </p>
 *
 * @author sunchao81
 * @since 2024-07-02
 */
@Controller
@RequestMapping("/local-test-record")
public class LocalTestRecordController {

    /**
     *
     */
    @Resource
    ILocalTestRecordService localTestRecordService;

    /**
     *
     */
    @Resource
    RequirementInfoService requirementInfoService;

    /**
     *
     */
    @Resource
    IRRequirementCaseService requirementCaseService;

    /**
     *
     */
    @Resource
    IMemberRelationService relationService;


    /**
     * 1、新建空间
     * 2、记录流水
     * 3、绑定用例
     * 4、绑定接口
     * @param param
     * @return
     */
    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value = "新增本地自测记录")
    public CommonResult<Boolean> add(@RequestBody LocalTestRecordParam param) {
        Guard.notEmpty(param.getGitUrl(), "入参不能为空");
        Guard.notEmpty(param.getGitBranch(), "入参不能为空");
        LocalTestRecord record = DtBeanUtils.getCreatBean(param,LocalTestRecord.class);
        record.setCaseSource(CaseSourceEnum.getByCode(param.getCaseSource()).getIndex());
        record.setCreator(param.getErp());
        RequirementInfo requirementInfo = createRequirementInfo(record);
        requirementCaseService.bindRequirementCase(requirementInfo.getId(),record);
        record.setRequirementInfoId(requirementInfo.getId());
        boolean result = localTestRecordService.save(record);
        return CommonResult.buildSuccessResult(result);
    }

    /**
     * 插件上报新建需求空间
     * @param param
     * @return
     */
    private RequirementInfo createRequirementInfo(LocalTestRecord param) {
        List<RequirementInfo> list = requirementInfoService.lambdaQuery().eq(RequirementInfo::getYn, DataYnEnum.VALID.getCode())
                .eq(RequirementInfo::getGitUrl, param.getGitUrl()).eq(RequirementInfo::getGitBranch, param.getGitBranch()).list();
        if(CollectionUtil.isEmpty(list)){
            RequirementInfo requirementInfo = new RequirementInfo().setType(RequirementTypeEnum.JAPI.getCode()).setStatus(STATUS_FINISHED)
                    .setName(param.getGitBranch() + "生成空间").setDescription(param.getGitBranch() + "生成空间")
                    .setGitUrl(param.getGitUrl()).setGitBranch(param.getGitBranch());
            requirementInfo.setYn(DataYnEnum.VALID.getCode());
            requirementInfo.setCreator(param.getCreator());
            requirementInfoService.save(requirementInfo);

            MemberRelation relation = requirementInfoService.newMemberRelation(param.getCreator(), requirementInfo.getId());
            relation.setResourceRole(ResourceRoleEnum.ADMIN.getCode());
            relationService.save(relation);
            return requirementInfo;
        }else {
            return list.get(0);
        }
    }

    @PostMapping("/addRequirement")
    @ResponseBody
    @ApiOperation(value = "新建需求空间")
    public CommonResult<Boolean> addRequirement(@RequestBody LocalRequirementParam param) {
        Guard.notEmpty(param.getGitUrl(), "入参不能为空");
        Guard.notEmpty(param.getGitBranch(), "入参不能为空");
        LocalTestRecord localTestRecord = new LocalTestRecord();
        BeanUtils.copyProperties(param,localTestRecord);
        localTestRecord.setCreator(param.getErp());
        createRequirementInfo(localTestRecord);
        return CommonResult.buildSuccessResult(false);
    }
}
