package com.jd.workflow.console.service.doc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dao.mapper.doc.InterfaceVersionMapper;
import com.jd.workflow.console.dao.mapper.doc.MethodModifyLogMapper;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.dto.doc.ListModifyLogDto;
import com.jd.workflow.console.dto.doc.MethodContentSnapshot;
import com.jd.workflow.console.dto.version.CompareMethodVersionDTO;
import com.jd.workflow.console.dto.version.MethodVersionDTO;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.doc.InterfaceVersion;
import com.jd.workflow.console.entity.doc.MethodModifyLog;
import com.jd.workflow.console.entity.doc.MethodVersionModifyLog;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.doc.IInterfaceVersionService;
import com.jd.workflow.console.service.doc.IMethodModifyLogService;
import com.jd.workflow.console.service.method.MethodModifyDeltaInfoService;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class MethodModifyLogServiceImpl extends ServiceImpl<MethodModifyLogMapper, MethodModifyLog> implements IMethodModifyLogService {

    @Autowired
    IMethodManageService methodManageService;
    @Autowired
    MethodModifyDeltaInfoService deltaInfoService;

    @Autowired
    InterfaceVersionServiceImpl interfaceVersionService;


    @Override
    public IPage<MethodModifyLog> listModifyLogs(ListModifyLogDto dto) {
        LambdaQueryWrapper<MethodModifyLog> qw = Wrappers.<MethodModifyLog>lambdaQuery().orderByDesc(MethodModifyLog::getId);
        qw.eq(MethodModifyLog::getInterfaceId,dto.getInterfaceId());
        qw.eq(MethodModifyLog::getMethodId,dto.getMethodId());
        qw.select(MethodModifyLog::getId,MethodModifyLog::getInterfaceId,MethodModifyLog::getMethodId,MethodModifyLog::getVersion,MethodModifyLog::getModified,MethodModifyLog::getModifier);
        return page(new Page<>(dto.getCurrent(), dto.getSize()), qw);
    }

    @Override
    public MethodModifyLog getDetailById(Long id) {
        return getById(id);
    }

    @Override
    public CompareMethodVersionDTO compareMethod(Long id) {

        MethodModifyLog modifyLog = getById(id);
        MethodManage method = methodManageService.getById(modifyLog.getMethodId());

        CompareMethodVersionDTO dto = new CompareMethodVersionDTO();
        dto.setInterfaceId(dto.getInterfaceId());
        dto.setMethodId(method.getId());
        dto.setMethodCode(method.getMethodCode());
        dto.setMethodName(method.getName());
        dto.setDesc(method.getDesc());
        dto.setType(method.getType());
        dto.setHttpMethod(method.getHttpMethod());

        LambdaQueryWrapper<MethodModifyLog> lqw = new LambdaQueryWrapper<>();
        lqw.gt(MethodModifyLog::getId,id);
        lqw.eq(MethodModifyLog::getMethodId,method.getId());
        lqw.orderByAsc(MethodModifyLog::getId).last("LIMIT 1");
        List<MethodModifyLog> list = list(lqw);
        if(!list.isEmpty()){
            dto.setVersionDto(getCompareDto(list.get(0),method));
        }else{
            dto.setVersionDto(interfaceVersionService.getLatestMethodVersion(method,modifyLog.getVersion(),false));
        }


        dto.setCompareVersionDto(getCompareDto(modifyLog,method));
        return dto;
    }

    @Override
    public void removeByInterfaceId(Long interfaceId) {
        LambdaQueryWrapper<MethodModifyLog> modifyLog = new LambdaQueryWrapper<>();
        modifyLog.in(MethodModifyLog::getInterfaceId, Collections.singletonList(interfaceId));
        remove(modifyLog);
    }

    @Override
    public void removeByMethodIds(List<Long> methodIds) {
        if(methodIds.isEmpty()) return;
        LambdaQueryWrapper<MethodModifyLog> modifyLog = new LambdaQueryWrapper<>();
        modifyLog.in(MethodModifyLog::getMethodId, methodIds);
        remove(modifyLog);
    }


    @Override
    public Boolean updateLogRemark(Long id, String remark){
        if (StringUtils.isNotBlank(remark)) {
            Guard.assertTrue(remark.length() <= 50, "请输入1~50位字符");
        }
        LambdaUpdateWrapper<MethodModifyLog> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MethodModifyLog::getId, id);
        wrapper.eq(MethodModifyLog::getYn, 1);
        wrapper.set(MethodModifyLog::getRemark, remark);
        return update(wrapper);

    }

    private MethodVersionDTO getCompareDto(MethodModifyLog modifyLog,MethodManage methodManage ){
        MethodVersionDTO dto = new MethodVersionDTO();
        dto.setVersionDesc(modifyLog.getVersion());
        MethodContentSnapshot snapshot = modifyLog.getMethodContentSnapshot();
        dto.setDesc(snapshot.getDesc());
        dto.setContent(snapshot.getContent());
        dto.setContentObject(JsonUtils.parse(snapshot.getContent()));
        if(InterfaceTypeEnum.HTTP.getCode().equals(methodManage.getType())){
            try{
                dto.setHttpMethod(JsonUtils.parse(snapshot.getContent(),HttpMethodModel.class).getInput().getMethod());
            }catch (Exception e){
                log.error("解析http方法失败",e);
            }

        }

        dto.setInputExample(snapshot.getInputExample());
        dto.setOutputExample(snapshot.getOutputExample());
        dto.setModified(modifyLog.getModified());
        dto.setModifier(modifyLog.getModifier());
        return dto;
    }

    public void recoveryMethod(Long id){
        MethodModifyLog modifyLog = getById(id);
        Guard.notEmpty(modifyLog,"无效的修改日志");
        MethodManage methodManage = methodManageService.getById(modifyLog.getMethodId());

        methodManage.setContent(modifyLog.getMethodContentSnapshot().getContent());

        methodManageService.fillMethodDigest(methodManage);
        HttpMethodModel model = JsonUtils.parse(methodManage.getContent(),HttpMethodModel.class);
        LambdaUpdateWrapper<MethodManage> luw = new LambdaUpdateWrapper<>();
        luw.eq(MethodManage::getId,methodManage.getId());
        luw.set(MethodManage::getContent,methodManage.getContent());
        luw.set(MethodManage::getDigest,methodManage.getDigest());
        luw.set(MethodManage::getPath,model.getInput().getUrl());

        methodManageService.update(luw);

        deltaInfoService.removeDelta(methodManage.getId());




    }
}
