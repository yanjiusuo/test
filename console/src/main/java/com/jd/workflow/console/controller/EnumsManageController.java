package com.jd.workflow.console.controller;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/7
 */

import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import com.jd.workflow.console.dto.MethodPropDTO;
import com.jd.workflow.console.dto.doc.InterfaceCountModel;
import com.jd.workflow.console.dto.errorcode.*;
import com.jd.workflow.console.dto.group.GroupResolveDto;
import com.jd.workflow.console.entity.errorcode.Enums;
import com.jd.workflow.console.entity.errorcode.REnumMethodProp;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.errorcode.IEnumPropService;
import com.jd.workflow.console.service.errorcode.IEnumsService;
import com.jd.workflow.console.service.errorcode.IREnumMethodPropService;
import com.jd.workflow.console.service.group.impl.AppGroupManageServiceImpl;
import com.jd.workflow.console.service.model.IApiModelGroupService;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/7
 */
@Slf4j
@RestController
@RequestMapping("/enumsManage")
@UmpMonitor
@Api(value = "枚举&错误码管理", tags = "枚举&错误码管理")
public class EnumsManageController {

    @Autowired
    private IEnumPropService enumPropService;

    @Autowired
    private IEnumsService enumsService;

    @Autowired
    private IREnumMethodPropService irEnumMethodPropService;

    @Autowired
    private IMethodManageService methodManageService;
    @Autowired
    AppGroupManageServiceImpl appGroupManageService;
    @Autowired
    private IApiModelGroupService apiModelGroupService;

    @GetMapping("/queryErrorCodeProp")
    @ApiOperation(value = "查看错误码")
    public CommonResult<List<EnumPropDTO>> queryErrorCodeProp(@RequestParam Long appId) {
        log.info("EnumsManageController queryErrorCodeProp appId={}", appId);

        return CommonResult.buildSuccessResult(enumPropService.queryErrorCodeProp(appId));
    }

    @PostMapping("/saveEnumProps")
    @ApiOperation(value = "保存错误码&枚举")
    public CommonResult<Boolean> saveEnumProps(@RequestBody SaveEnumDTO saveEnumDTO) {
        log.info("EnumsManageController saveErrorCode saveErrorCodeDTO={}", JsonUtils.toJSONString(saveEnumDTO));

        return CommonResult.buildSuccessResult(enumPropService.saveEnumProps(saveEnumDTO));
    }

    @PostMapping("/queryMethodProps")
    @ApiOperation(value = "查看接口属性")
    public CommonResult<List<MethodPropDTO>> queryMethodProps(@RequestBody MethodPropParam methodPropParam) {
        log.info("EnumsManageController queryMethodProps methodPropParam={}", JSON.toJSONString(methodPropParam));
        if (methodPropParam.getCurrent() == null) {
            methodPropParam.setCurrent(1L);
        }
        if (methodPropParam.getSize() == null) {
            methodPropParam.setSize(20L);
        }
        if (methodPropParam.getAppId() == null || methodPropParam.getAppId() < 1) {
            throw new BizException("应用id不能为空");
        }

        return CommonResult.buildSuccessResult(methodManageService.getTopProp(methodPropParam));
    }

    @PostMapping("/bindEnum")
    @ApiOperation(value = "枚举|错误码绑定属性")
    public CommonResult<Boolean> bindEnum(@RequestBody BindPropParam bindPropParam) {
        log.info("EnumsManageController bindEnum bindPropParam={}", JsonUtils.toJSONString(bindPropParam));

        return CommonResult.buildSuccessResult(irEnumMethodPropService.bindEnum(bindPropParam));
    }


    @PostMapping("/bindEnumList")
    @ApiOperation(value = "获取枚举|错误码绑定属性列表")
    public CommonResult<List<REnumMethodProp>> bindEnumList(@RequestBody BindPropParam bindPropParam) {
        log.info("EnumsManageController bindEnumList bindPropParam={}", JsonUtils.toJSONString(bindPropParam));

        return CommonResult.buildSuccessResult(irEnumMethodPropService.bindEnumList(bindPropParam));
    }

    @PostMapping("/deleteBindEnum")
    @ApiOperation(value = "删除枚举|错误码绑定属性")
    public CommonResult<Boolean> deleteBindEnum(@RequestBody BindPropParam bindPropParam) {
        log.info("EnumsManageController deleteBindEnum bindPropParam={}", JsonUtils.toJSONString(bindPropParam));

        return CommonResult.buildSuccessResult(irEnumMethodPropService.deleteBindEnum(bindPropParam));
    }

    @GetMapping("/queryAllEnums")
    @ApiOperation(value = "查看错误码+枚举列表")
    public CommonResult<List<EnumDTO>> queryAllEnums(@RequestParam(required = true) Long appId) {
        log.info("EnumsManageController queryAllEnums appId={}", appId);

        return CommonResult.buildSuccessResult(enumsService.queryAllEnums(appId));
    }


    @GetMapping("/queryEnums")
    @ApiOperation(value = "查看枚举列表")
    public CommonResult<List<EnumDTO>> queryEnums(@RequestParam(required = true) Long appId) {
        log.info("EnumsManageController queryEnums appId={}", appId);

        return CommonResult.buildSuccessResult(enumsService.queryEnumsByType(appId, 1));
    }

    @GetMapping("/queryEnumProp")
    @ApiOperation(value = "查看枚举值")
    public CommonResult<List<EnumPropDTO>> queryEnumProp(@RequestParam(required = true) Long appId, @RequestParam(required = true) Long enumId) {
        log.info("EnumsManageController queryErrorCodeProp appId={}", appId);

        return CommonResult.buildSuccessResult(enumPropService.queryEnumProp(appId, enumId));
    }

    @GetMapping("/initMethodProp")
    @ApiOperation(value = "初始化属性")
    public CommonResult<Boolean> initMethodProp() {
        log.info("EnumsManageController initMethodProp ");
        methodManageService.initAllMethodProps();
        return CommonResult.buildSuccessResult(true);
    }

    @PostMapping("/deleteEnumProp")
    @ApiOperation(value = "删除枚举值")
    public CommonResult<Boolean> deleteEnumProp(@RequestBody EnumPropDTO enumPropDTO) {
        log.info("EnumsManageController deleteEnumProp enumPropDTO={}", JsonUtils.toJSONString(enumPropDTO));

        return CommonResult.buildSuccessResult(enumPropService.deleteEnumProp(enumPropDTO));
    }


    @PostMapping("/deleteEnum")
    @ApiOperation(value = "删除枚举")
    public CommonResult<Boolean> deleteEnum(@RequestBody EnumDTO enumDTO) {
        log.info("EnumsManageController deleteEnum enumDTO={}", JsonUtils.toJSONString(enumDTO));

        return CommonResult.buildSuccessResult(enumsService.deleteEnum(enumDTO));
    }

    @PostMapping("/saveEnum")
    @ApiOperation(value = "保存枚举")
    public CommonResult<Boolean> saveEnum(@RequestBody EnumDTO enumDTO) {
        log.info("EnumsManageController deleteEnum enumDTO={}", JsonUtils.toJSONString(enumDTO));
        enumDTO.setEnumType(1);
        if (Objects.isNull(enumDTO.getAppId())) {
            throw new BizException("应用id不能为空");
        }


        return CommonResult.buildSuccessResult(enumsService.saveEnum(enumDTO));
    }

    @PostMapping("/deleteEnums")
    @ApiOperation(value = "批量删除枚举")
    public CommonResult<Boolean> deleteEnums(@RequestBody DeleteEnumPropDTO deleteEnumPropDTO) {
        log.info("EnumsManageController deleteEnums deleteEnumPropDTO={}", JsonUtils.toJSONString(deleteEnumPropDTO));
//        if (Objects.isNull(deleteEnumPropDTO.getAppId())) {
//            throw new BizException("应用id不能为空");
//        }

        return CommonResult.buildSuccessResult(enumPropService.deleteEnumProps(deleteEnumPropDTO));
    }

    @GetMapping("/getAllEnumModelBeanTree")
    @ApiOperation(value = "获取全部")
    public CommonResult<EnumModelBeansDTO> getAllEnumModelBeanTree(@RequestParam(required = true) Long appId) {
        log.info("EnumsManageController getAllEnumModelBeanTree appID:{}", appId);
        EnumModelBeansDTO enumModelBeansDTO = new EnumModelBeansDTO();
        {
            MethodGroupTreeDTO methodGroupTreeDTO = apiModelGroupService.findMethodGroupTree(appId, "", true);
            enumModelBeansDTO.setModelTree(methodGroupTreeDTO);

            if (CollectionUtils.isEmpty(methodGroupTreeDTO.getTreeModel().allMethods())) {
                enumModelBeansDTO.setModelCount(0);
            } else {
                enumModelBeansDTO.setModelCount(methodGroupTreeDTO.getTreeModel().allMethods().size());
            }

        }
        GroupResolveDto dto = new GroupResolveDto();
        dto.setId(appId);
        dto.setType(1);
        {
            List<InterfaceCountModel> countModels = appGroupManageService.getRootGroups(dto, 9);
            for (InterfaceCountModel countModel : countModels) {
                MethodGroupTreeDTO methodGroupTreeDTO1 = appGroupManageService.findMethodGroupTree(dto, countModel.getId());
                countModel.setChildren(methodGroupTreeDTO1.getTreeModel().getTreeItems());
                countModel.initKey();
            }
            enumModelBeansDTO.setBeansList(countModels);
            if (CollectionUtils.isEmpty(countModels)) {
                enumModelBeansDTO.setBeanCount(0);
            } else {

                enumModelBeansDTO.setBeanCount(countModels.stream().mapToInt(InterfaceCountModel::getCount).sum());
            }
        }
        {

            List<EnumDTO> enumDTOList = enumsService.queryAllEnums(appId);
            enumModelBeansDTO.setAllEnumList(enumDTOList);
            enumModelBeansDTO.setEnumCount(enumDTOList.size());
        }


        return CommonResult.buildSuccessResult(enumModelBeansDTO);
    }

    @GetMapping("/queryEnumInfo")
    @ApiOperation(value = "查询枚举详情")
    public CommonResult<EnumFullDTO> queryEnumInfo(@RequestParam(required = true) Long appId, @RequestParam(required = true) Long enumId) {
        log.info("EnumsManageController queryEnumInfo appId={},enumId={}", appId, enumId);

        Enums enums = enumsService.getById(enumId);
        EnumFullDTO enumFullDTO = new EnumFullDTO();


        BeanUtils.copyProperties(enums, enumFullDTO);
        List<EnumPropDTO> enumPropDTOList = enumPropService.queryEnumProp(appId, enumId);
        enumFullDTO.setEnumPropDTOList(enumPropDTOList);


        return CommonResult.buildSuccessResult(enumFullDTO);

    }

    @PostMapping("/saveEnumInfo")
    @ApiOperation(value = "保存枚举详情")
    public CommonResult<EnumFullDTO> saveEnumInfo(@RequestBody EnumFullDTO enumFullDTO) {
        Boolean result = enumsService.saveEnum(enumFullDTO);
        SaveEnumDTO saveEnumDTO = new SaveEnumDTO();
        saveEnumDTO.setAppId(enumFullDTO.getAppId());
        saveEnumDTO.setEnumId(enumFullDTO.getId());
        saveEnumDTO.setEnumPropDTOList(enumFullDTO.getEnumPropDTOList());
        Boolean result2 = enumPropService.saveEnumProps(saveEnumDTO);

        return CommonResult.buildSuccessResult(enumFullDTO);
    }
}
