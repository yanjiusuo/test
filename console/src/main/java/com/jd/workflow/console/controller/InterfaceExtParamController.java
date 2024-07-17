package com.jd.workflow.console.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.dto.PublicParamsDTO;
import com.jd.workflow.console.entity.InterfaceExtParam;
import com.jd.workflow.console.service.InterfaceExtService;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;

import io.swagger.annotations.ApiOperation;

@Slf4j
@RestController
@UmpMonitor
@RequestMapping("/interfaceExt")
@Api(tags="接口分组管理")
public class InterfaceExtParamController {

    @Resource
    InterfaceExtService publicParamsService;

    /**
     * 新增
     * 入参:
     * 出参: id
     *
     * @date: 2022/9/13 17:55
     * @author shuhcang21
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加公共参数")
    public CommonResult<Long> add(@RequestBody PublicParamsDTO publicParamsDTO) {
        log.info("InterfaceManageController add query={}", JsonUtils.toJSONString(publicParamsDTO));
        //service层
        Long ref = publicParamsService.add(publicParamsDTO);
        //出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * 修改
     * 入参:
     * 出参: id
     *
     * @date: 2022/9/13 18:20
     * @author shuhcang21
     */
    @PostMapping("/edit")
    @ApiOperation(value = "修改公共参数")
    public CommonResult<Long> edit(@RequestBody PublicParamsDTO publicParamsDTO) {
        log.info("InterfaceManageController add query={}", JsonUtils.toJSONString(publicParamsDTO));
        //service层
        Long ref = publicParamsService.edit(publicParamsDTO);
        //出参
        return CommonResult.buildSuccessResult(ref);
    }

    @GetMapping("/getByInterfaceId")
    @ApiOperation(value = "获取jsf接口公共参数")
    public CommonResult<PublicParamsDTO> getByInterfaceId(Long interfaceId) {
        log.info("InterfaceManageController add query={}", interfaceId);
        //service层
        InterfaceExtParam ref = publicParamsService.getByInterfaceId(interfaceId);
        if (ref == null) {
            return CommonResult.buildSuccessResult(null);
        }
        PublicParamsDTO dto = new PublicParamsDTO();
        if (!StringUtils.isBlank(ref.getContent())) {
            dto.setContent(JsonUtils.parseArray(ref.getContent(), JsonType.class));
        }
        dto.setId(ref.getId());
        dto.setInterfaceId(ref.getInterfaceId());
        dto.setType(ref.getType());
        //出参
        return CommonResult.buildSuccessResult(dto);
    }

    /**
     * 删除
     * 入参: id
     * 出参: boolean
     *
     * @date: 2022/9/13 18:40
     * @author shuchang21
     */
    @PostMapping("/remove")
    public CommonResult<Boolean> remove(@RequestParam("id") Long id) {
        //service层
        Boolean ref = publicParamsService.remove(id);
        //4.出参
        if (ref) {
            return CommonResult.buildSuccessResult(ref);
        } else {
            return new CommonResult(ServiceErrorEnum.UPDATE_DB_ERROR.getCode(), "该接口下存在可用方法，拒绝删除接口", ref);
        }
    }

    /**
     * 分页查询
     *
     * @param
     * @return
     * @date: 2022/9/13 20:12
     * @author shuchang21
     */
    @GetMapping("/page")
    public CommonResult<Page<InterfaceExtParam>> page(@RequestParam("page") Integer page) {
        //3.service层
        Page<InterfaceExtParam> result = publicParamsService.page(page);
        //4.出参
        return CommonResult.buildSuccessResult(result);
    }
}
