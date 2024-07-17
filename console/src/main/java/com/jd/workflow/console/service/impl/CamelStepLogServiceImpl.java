package com.jd.workflow.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.PublishEnum;
import com.jd.workflow.console.base.enums.ResourceTypeEnum;
import com.jd.workflow.console.dao.mapper.CamelStepLogMapper;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.entity.CamelStepLog;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MemberRelation;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.PublishManage;
import com.jd.workflow.console.helper.ProjectHelper;
import com.jd.workflow.console.service.ICamelStepLogService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMemberRelationService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.IPublishManageService;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 项目名称：example
 * 类 名 称：CamelStepLogServiceImpl
 * 类 描 述：日志service接口实现类
 * 创建时间：2022-06-08 09:06
 * 创 建 人：wangxiaofei8
 */
@Service
@Slf4j
public class CamelStepLogServiceImpl extends ServiceImpl<CamelStepLogMapper, CamelStepLog> implements ICamelStepLogService {

    @Resource
    private IInterfaceManageService interfaceManageService;


    @Resource
    private IMethodManageService methodManageService;


    @Resource
    private IPublishManageService publishManageService;

    @Resource
    private IMemberRelationService memberRelationService;

    @Resource
    private ProjectHelper projectHelper;

    /**
     * 查询日志
     * @param reqDTO
     * @return
     */
    @Override
    public CamelLogListDTO queryCamleStepLog(CamelLogReqDTO reqDTO) {
        CamelLogListDTO result = new CamelLogListDTO();
        //根据日志id单个查询
        if(reqDTO.getId()!=null){
            CamelStepLog obj = this.getById(reqDTO.getId());
            if(obj!=null){
               result.addCamelLogDto(convertToCamelLogDTO(obj,true,null,null));
            }
            return result;
        }
        LambdaQueryWrapper<CamelStepLog> query = Wrappers.<CamelStepLog>lambdaQuery().orderByDesc(CamelStepLog::getId);
        if(reqDTO.getMethodId()!=null){
            query.eq(CamelStepLog::getMethodId,reqDTO.getMethodId());
        }
        if(reqDTO.getLogLevel()!=null){
            query.eq(CamelStepLog::getLogLevel,reqDTO.getLogLevel());
        }
        if(reqDTO.getVersion()!=null){
            query.eq(CamelStepLog::getVersion,reqDTO.getVersion());
        }
        if(reqDTO.getStartDate()!=null){
            query.ge(CamelStepLog::getCreated,reqDTO.getStartDate());
        }
        if(reqDTO.getEndDate()!=null){
            query.le(CamelStepLog::getCreated,reqDTO.getEndDate());
        }
        Page<CamelStepLog> pageResult = this.page(initPage(reqDTO,10), query);
        if(pageResult!=null&& CollectionUtils.isNotEmpty(pageResult.getRecords())){
            result.setTotalCnt(Long.valueOf(pageResult.getTotal()));
            List<Long> methodIds = pageResult.getRecords().stream()
                    .map(o -> Long.valueOf(o.getMethodId())).collect(Collectors.toSet()).stream()
                    .collect(Collectors.toList());
            //接口信息
            Map<Long, InterfaceManage> interfaceManageMap = new HashMap<>();
            //获取方法名称
            Map<Long,String> methodName = getMethodName(methodIds,interfaceManageMap);
            //封装返回dto
            result.setList(pageResult.getRecords().stream().map(o->convertToCamelLogDTO(o,false
                    , methodName.get(Long.valueOf(o.getMethodId())),interfaceManageMap.get(Long.valueOf(o.getMethodId())).getName())).collect(Collectors.toList()));
        }
        return result;
    }

    /**
     * 查询接口
     * @param reqDTO
     * @return
     */
    @Override
    public CamelLogConditionDTO queryLogInterfaceCondition(CamelLogReqDTO reqDTO) {
        //判断是否租户管理员
        Boolean tenantAdmin = memberRelationService.checkTenantAdmin(UserSessionLocal.getUser().getUserId());
        List<Long> interfaceIds = null;
        if(!BooleanUtils.isTrue(tenantAdmin))
        {
            LambdaQueryWrapper<MemberRelation> mrQw = new LambdaQueryWrapper<>();
            mrQw.eq(MemberRelation::getUserCode,UserSessionLocal.getUser().getUserId());
            mrQw.in(MemberRelation::getResourceType, ResourceTypeEnum.INTERFACE.getCode(),ResourceTypeEnum.ORCHESTRATION.getCode());
            mrQw.eq(MemberRelation::getYn, DataYnEnum.VALID.getCode());
            List<MemberRelation> memberRelations = memberRelationService.list(mrQw);
            if(CollectionUtils.isNotEmpty(memberRelations)){
                interfaceIds = memberRelations.stream().map(x -> x.getResourceId()).collect(Collectors.toList());
            }else{
                CamelLogConditionDTO result = new CamelLogConditionDTO();
                result.setTotalCnt(0L);
                result.setList(new ArrayList<>());
                return result;
            }
        }
        LambdaQueryWrapper<InterfaceManage> qw = new LambdaQueryWrapper<>();
        qw.eq(InterfaceManage::getYn,DataYnEnum.VALID.getCode());
        if(StringUtils.isNotBlank(reqDTO.getName())) {
            qw.likeRight(InterfaceManage::getName,reqDTO.getName());
        }
        if(CollectionUtils.isNotEmpty(interfaceIds)){
            qw.in(InterfaceManage::getId,interfaceIds);
        }
        qw.inSql(InterfaceManage::getId, "SELECT DISTINCT(m.interface_id) from `method_manage` m WHERE m.published="+PublishEnum.YES.getCode()+" and m.yn="+ DataYnEnum.VALID.getCode());
        Page<InterfaceManage> pageResult = interfaceManageService.page(initPage(reqDTO, 20), qw);
        CamelLogConditionDTO result = new CamelLogConditionDTO();
        if(pageResult!=null&& CollectionUtils.isNotEmpty(pageResult.getRecords())){
            result.setTotalCnt(Long.valueOf(pageResult.getTotal()));
            pageResult.getRecords().forEach(o->result.addElement(o.getId(),o.getName()));
        }
        return result;
    }

    /**
     * 查询发布的方法
     * @param reqDTO
     * @return
     */
    @Override
    public CamelLogConditionDTO queryLogMethodCondition(CamelLogQueryDTO reqDTO) {
        if(reqDTO.getPublished() == null){
            reqDTO.setPublished(1);
        }
        CamelLogConditionDTO result = new CamelLogConditionDTO();
        LambdaQueryWrapper<MethodManage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MethodManage::getInterfaceId,reqDTO.getInterfaceId());
        queryWrapper.eq(reqDTO.getPublished() == 1, MethodManage::getPublished, PublishEnum.YES.getCode());
        if(StringUtils.isNotBlank(reqDTO.getName())){
            queryWrapper.like(MethodManage::getName,reqDTO.getName());
        }
        queryWrapper.setEntityClass(MethodManage.class).select(new Predicate<TableFieldInfo>() {
            @Override
            public boolean test(TableFieldInfo tableFieldInfo) {
                String[] bigTextFields = new String[]{"content","doc_info"};
                return Arrays.asList(bigTextFields).indexOf(tableFieldInfo.getColumn()) == -1;
               // return !tableFieldInfo.getColumn() .equalsIgnoreCase("content");
            }
        });
        // 最多显示200条
        queryWrapper.last("LIMIT 200");
        List<MethodManage> list = methodManageService.list(queryWrapper);
        if(CollectionUtils.isNotEmpty(list)){
            result.setTotalCnt(Long.valueOf(list.size()));
            List<Long> parentIds = new ArrayList<>();
            Map<Long, MethodManage> ipMapping = new HashMap<>();
            list.forEach(o->{
                if(o.getParentId()!=null){
                    if(!parentIds.contains(o.getParentId())){
                        parentIds.add(o.getParentId());
                    }
                    ipMapping.put(o.getId(),o);
                }else{
                    result.addElement(o.getId(),o.getName());
                }
            });
            if(parentIds.size()>0){
                Map<Long, MethodManage> parentResult = filterBigFieldByIds(parentIds).stream().collect(Collectors.toMap(MethodManage::getId, MethodManage -> MethodManage));
                ipMapping.forEach((k,v)->{
                    if(parentResult.containsKey(v.getParentId())){
                        result.addElement(k,parentResult.get(v.getParentId()).getName()+"-"+v.getCallEnv());
                    }

                });
            }
        }
        return result;
    }

    /**
     * 查询发布版本
     * @param reqDTO
     * @return
     */
    @Override
    public CamelLogConditionDTO queryLogVersionCondition(CamelLogReqDTO reqDTO) {
        CamelLogConditionDTO result = new CamelLogConditionDTO();
        LambdaQueryWrapper<PublishManage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PublishManage::getRelatedMethodId,reqDTO.getMethodId());
        queryWrapper.setEntityClass(PublishManage.class).select(new Predicate<TableFieldInfo>() {
            @Override
            public boolean test(TableFieldInfo tableFieldInfo) {
                return !tableFieldInfo.getColumn() .equalsIgnoreCase("content");
            }
        });
        Page<PublishManage> pageResult = publishManageService.page(initPage(reqDTO, 20), queryWrapper);
        if(pageResult!=null&& CollectionUtils.isNotEmpty(pageResult.getRecords())){
            result.setTotalCnt(Long.valueOf(pageResult.getTotal()));
            pageResult.getRecords().forEach(o->result.addElement(Long.valueOf(o.getVersionId()),o.getVersionId().toString()));
        }
        return result;
    }


    /**
     * dto转换
     * @param obj
     * @param fillNameValue
     * @return
     */
    private CamelLogDTO convertToCamelLogDTO(CamelStepLog obj,boolean fillNameValue,String viewName,String interfaceName){
        CamelLogDTO dto = new CamelLogDTO();
        dto.setId(obj.getId());
        dto.setCreated(obj.getCreated());
        dto.setLogContent(obj.getLogContent());
        dto.setMethodId(obj.getMethodId());
        dto.setLogLevel(obj.getLogLevel());
        dto.setPublishUrl(projectHelper.getPublishUrl(obj.getMethodId()));
        dto.setVersion(obj.getVersion());
        if(fillNameValue){
            MethodManage methodManage = methodManageService.getBaseMapper().selectById(obj.getMethodId());
            if(InterfaceTypeEnum.WEB_SERVICE.getCode().equals(methodManage.getType())){
                MethodManage parentHttp = methodManageService.getBaseMapper().selectById(methodManage.getParentId());
                dto.setName(parentHttp.getName()+"-"+methodManage.getCallEnv());
            }else{
                dto.setName(methodManage.getName());
            }
            InterfaceManage interfaceManage = interfaceManageService.getBaseMapper().selectById(methodManage.getInterfaceId());
            dto.setInterfaceName(interfaceManage.getName());
        }
        if(viewName!=null){
            dto.setName(viewName);
        }
        if(interfaceName!=null){
            dto.setInterfaceName(interfaceName);
        }
        return dto;
    }

    /**
     * 封装分页数据
     * @param reqDTO
     * @return
     */
    private Page initPage(CamelLogReqDTO reqDTO,Integer maxPageSize){
        Integer currentPage = 1;
        if(reqDTO.getCurrentPage()!=null&&reqDTO.getCurrentPage()>currentPage){
            currentPage = reqDTO.getCurrentPage();
        }
        Integer pageSize = maxPageSize;
        if(reqDTO.getPageSize()!= null && reqDTO.getPageSize()>0 && reqDTO.getPageSize()<pageSize){
            pageSize = reqDTO.getPageSize();
        }
        return  new Page(currentPage,pageSize);
    }


    /**
     * 查询名称
     * @param methodIds
     * @return
     */
    private Map<Long, String> getMethodName(List<Long> methodIds,Map<Long,InterfaceManage> interfaceManageMap){
        //过滤大字段查询方法
        List<MethodManage> methodManages = filterBigFieldByIds(methodIds);
        Map<Long, String> methodNames = new HashMap<>();
        List<Long> parentIds = new ArrayList<>();
        Map<Long, Long> interfaceIdMapping = new HashMap<>();
        Map<Long, MethodManage> ipMapping = new HashMap<>();
        methodManages.forEach(o->{
            if(o.getParentId()!=null){
                if(!parentIds.contains(o.getParentId())){
                    parentIds.add(o.getParentId());
                }
                ipMapping.put(o.getId(),o);
            }else{
                methodNames.put(o.getId(),o.getName());
            }
            interfaceIdMapping.put(o.getId(),o.getInterfaceId());
        });
        //查询接口信息
        Map<Long, InterfaceManage> interfaceManagersByIds = getInterfaceManagersByIds(interfaceIdMapping.values());
        interfaceIdMapping.forEach((k,v)->{
            interfaceManageMap.put(k,interfaceManagersByIds.get(v));
        });
        if(parentIds.size()>0){
            Map<Long, MethodManage> parentResult = filterBigFieldByIds(parentIds).stream().collect(Collectors.toMap(MethodManage::getId, MethodManage -> MethodManage));
            ipMapping.forEach((k,v)->{
                methodNames.put(k,parentResult.get(v.getParentId()).getName()+"-"+v.getCallEnv());
            });
        }
        return methodNames;
    }


    /**
     * 批量获取方法实体
     * @param methodIds
     * @return
     */
    private List<MethodManage> filterBigFieldByIds(List<Long> methodIds){
        LambdaQueryWrapper<MethodManage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(MethodManage::getId,methodIds);
        queryWrapper.setEntityClass(MethodManage.class).select(new Predicate<TableFieldInfo>() {
            @Override
            public boolean test(TableFieldInfo tableFieldInfo) {
                return !tableFieldInfo.getColumn() .equalsIgnoreCase("content");
            }
        });
        return methodManageService.getBaseMapper().selectList(queryWrapper);
    }

    /**
     * 批量获得接口实体
     * @param interfaceIds
     * @return
     */
    private Map<Long,InterfaceManage> getInterfaceManagersByIds(Collection<Long> interfaceIds){
        LambdaQueryWrapper<InterfaceManage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(InterfaceManage::getId,new HashSet<>(interfaceIds));
        queryWrapper.setEntityClass(InterfaceManage.class).select(new Predicate<TableFieldInfo>() {
            @Override
            public boolean test(TableFieldInfo tableFieldInfo) {
                return !tableFieldInfo.getColumn() .equalsIgnoreCase("env");
            }
        });
        return Optional.ofNullable(interfaceManageService.getBaseMapper().selectList(queryWrapper))
                .orElse(new ArrayList<>()).stream().collect(Collectors.toMap(InterfaceManage::getId, Function.identity()));
    }
}
