package com.jd.workflow.console.service.param.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.up.portal.login.interceptor.UpLoginContextHelper;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.controller.utils.DtBeanUtils;
import com.jd.workflow.console.dto.requirement.ParamBuilderScriptParam;
import com.jd.workflow.console.entity.param.ParamBuilderScript;
import com.jd.workflow.console.dao.mapper.param.ParamBuilderScriptMapper;
import com.jd.workflow.console.service.param.IParamBuilderScriptService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.soap.common.lang.Guard;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 * 入参构建脚本 服务实现类
 * </p>
 *
 * @author sunchao81
 * @since 2024-05-13
 */
@Service
public class ParamBuilderScriptServiceImpl extends ServiceImpl<ParamBuilderScriptMapper, ParamBuilderScript> implements IParamBuilderScriptService {

    @Override
    public Page listPage(ParamBuilderScriptParam param) {
        Page<ParamBuilderScript> page = this.lambdaQuery().eq(ParamBuilderScript::getYn, DataYnEnum.VALID.getCode())
                .eq(Objects.nonNull(param.getAppId()), ParamBuilderScript::getAppId, param.getAppId())
                .eq(Objects.nonNull(param.getScriptSource()), ParamBuilderScript::getScriptSource, param.getScriptSource())
                .eq(Objects.nonNull(param.getType()), ParamBuilderScript::getType, param.getType())
                .like(Objects.nonNull(param.getScriptName()), ParamBuilderScript::getScriptName, param.getScriptName())
                .like(Objects.nonNull(param.getCreator()), ParamBuilderScript::getCreator, param.getCreator())
                .select(ParamBuilderScript::getId, ParamBuilderScript::getScriptName, ParamBuilderScript::getType,
                        ParamBuilderScript::getCreator, ParamBuilderScript::getCreated)
                .orderByDesc(ParamBuilderScript::getCreated)
                .page(new Page(param.getCurrent(), param.getSize()));
        return page;
    }

    @Override
    public boolean saveScript(ParamBuilderScript param) {
        Guard.notEmpty(param, "入参不能为空");
        Guard.notEmpty(param.getScriptName(), "请输入物料模板名称");
        Guard.notEmpty(param.getAppId(), "未关联应用");
        Guard.assertTrue(param.getScriptName().length() <= 50, "请输入1~50位字符");
        Integer count = this.lambdaQuery().eq(ParamBuilderScript::getAppId, param.getAppId())
                .eq(ParamBuilderScript::getScriptName, param.getScriptName())
                .eq(ParamBuilderScript::getYn, DataYnEnum.VALID.getCode())
                .count();
        Guard.assertTrue(count == 0, "物料模板名称已存在");
        Guard.notEmpty(param.getScriptContent(), "请输入物料模板生成串");
        ParamBuilderScript createBean = DtBeanUtils.getCreatBean(param, ParamBuilderScript.class);
        createBean.setScriptSource(1);
        return this.save(createBean);
    }

    @Override
    public boolean editScript(ParamBuilderScript param) {
        // 判断名称是否已存在
        Integer count = this.lambdaQuery().eq(ParamBuilderScript::getAppId, param.getAppId())
                .eq(ParamBuilderScript::getScriptName, param.getScriptName())
                .eq(ParamBuilderScript::getYn, DataYnEnum.VALID.getCode())
                .ne(ParamBuilderScript::getId, param.getId())
                .count();
        Guard.assertTrue(count == 0, "物料模板名称已存在");
        ParamBuilderScript updateBean = DtBeanUtils.getUpdateBean(param, ParamBuilderScript.class);
        return this.updateById(updateBean);
    }

    @Override
    public ParamBuilderScript copy(Long id) {
        ParamBuilderScript paramBuilderScript = this.getById(id);
        Guard.assertTrue(Objects.nonNull(paramBuilderScript), "复制失败，模版不存在");
        // 复制名称
        String newName = paramBuilderScript.getScriptName() + "-副本";
        Optional<ParamBuilderScript> existingRecord = this.findByScriptName(newName, paramBuilderScript.getAppId());
        int counter = 1;
        while (existingRecord.isPresent()) {
            newName = paramBuilderScript.getScriptName() + "-副本" + counter++;
            existingRecord = this.findByScriptName(newName, paramBuilderScript.getAppId());
        }
        ParamBuilderScript newParamBuilderScript = new ParamBuilderScript();
        newParamBuilderScript.setScriptName(newName);
        newParamBuilderScript.setScriptContent(paramBuilderScript.getScriptContent());
        newParamBuilderScript.setScriptSource(paramBuilderScript.getScriptSource());
        newParamBuilderScript.setType(paramBuilderScript.getType());
        newParamBuilderScript.setAppId(paramBuilderScript.getAppId());
        newParamBuilderScript.setScriptResult(paramBuilderScript.getScriptResult());
        newParamBuilderScript.setCreator(UpLoginContextHelper.getUserPin());
        return newParamBuilderScript;
    }

    /**
     *
     * @param name
     * @param appId
     * @return
     */
    private Optional<ParamBuilderScript> findByScriptName(String name, Long appId) {
        return this.lambdaQuery().eq(ParamBuilderScript::getAppId, appId)
                .eq(ParamBuilderScript::getScriptName, name)
                .eq(ParamBuilderScript::getYn, DataYnEnum.VALID.getCode())
                .oneOpt();
    }
}
