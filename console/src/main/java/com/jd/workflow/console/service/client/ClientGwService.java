package com.jd.workflow.console.service.client;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.ResourceTypeEnum;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.EnvModel;
import com.jd.workflow.console.dto.client.InterfaceOutDto;
import com.jd.workflow.console.dto.client.MethodOutDto;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MemberRelation;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IInterfaceMethodGroupService;
import com.jd.workflow.console.service.IMemberRelationService;
import com.jd.workflow.console.service.impl.AppInfoServiceImpl;
import com.jd.workflow.console.service.impl.InterfaceManageServiceImpl;
import com.jd.workflow.console.service.impl.MemberRelationServiceImpl;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.console.service.remote.EasyMockRemoteService;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 第三方服务
 */
@Service
public class ClientGwService {

    public static String METHOD_DOC_URL_TEMPLATE = "http://console.paas.jd.com/idt/fe-app-view/demandManage/%s?methodId=%s";
    @Autowired
    InterfaceManageServiceImpl interfaceManageService;
    @Autowired
    MethodManageServiceImpl methodManageService;
    @Autowired
    AppInfoServiceImpl appInfoService;
    @Autowired
    IMemberRelationService memberRelationService;
    @Resource
    private IInterfaceMethodGroupService iInterfaceMethodGroupService;

    public InterfaceOutDto  queryInterfaceById(Long interfaceId){
        InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
        Guard.notEmpty(interfaceManage,"无效的接口id");
        fixInterfaceMembers(Collections.singletonList(interfaceManage));
        return convertToDto(interfaceManage);
    }
    public Page<InterfaceOutDto>  queryInterfaceIds(Long created, Long current, Long pageSize, Integer type){
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceManage::getType,type);
        if(created != null){
            lqw.and(child->{
                child.or(new Consumer<LambdaQueryWrapper<InterfaceManage>>() {
                    @Override
                    public void accept(LambdaQueryWrapper<InterfaceManage> wrapper) {
                        wrapper.ge(InterfaceManage::getCreated,new Date(created));
                    }
                }).or(new Consumer<LambdaQueryWrapper<InterfaceManage>>() {
                    @Override
                    public void accept(LambdaQueryWrapper<InterfaceManage> wrapper) {
                        wrapper.ge(InterfaceManage::getModified,new Date(created));
                    }
                });
            });

        }
        interfaceManageService.excludeBigTextFiled(lqw);
        Page<InterfaceManage> page = interfaceManageService.page(new Page<>(current, pageSize), lqw);
        fixInterfaceMembers(page.getRecords());
        Page<InterfaceOutDto> dto = new Page<>(page.getCurrent(),page.getSize(),page.getTotal());
        List<InterfaceOutDto> records = page.getRecords().stream().map(item->{

            return convertToDto(item);
        }).collect(Collectors.toList());
        dto.setRecords(records);
        return dto;
    }
    private InterfaceOutDto convertToDto(InterfaceManage interfaceManage){
        InterfaceOutDto ret = new InterfaceOutDto();


        BeanUtils.copyProperties(interfaceManage,ret);
        if(StringUtils.isNotBlank(interfaceManage.getEnv())){
            try{
                ret.setEnvList(JsonUtils.parseArray(interfaceManage.getEnv(), EnvModel.class));
            }catch (Exception e){
            }

        }
        ret.setMembers(interfaceManage.getUserCode());
        return ret;

    }
    private void fixInterfaceMembers(List<InterfaceManage> interfaces){
        Set<Long> appIds = interfaces.stream().filter(vs -> vs.getAppId() != null).map(vs -> vs.getAppId()).collect(Collectors.toSet());
        List<AppInfo> appInfos = new ArrayList<>();
        if(!appIds.isEmpty()){
            appInfos = appInfoService.listByIds(appIds);
        }
        Map<Long, List<AppInfo>> id2Apps = appInfos.stream().collect(Collectors.groupingBy(AppInfo::getId));
        final Map<Long, List<MemberRelation>> interfaceId2Relations = getInterfaceRelations(interfaces);
        for (InterfaceManage record : interfaces) {
            List<String> members = new ArrayList<>();
            if(record.getAppId() != null){
                List<AppInfo> apps = id2Apps.get(record.getAppId());
                if(!ObjectHelper.isEmpty(apps)){
                     AppInfo appInfo = apps.get(0);
                     AppInfoDTO dto = new AppInfoDTO();
                     members.addAll(dto.splitMembers(appInfo.getMembers()));
                }

            }
            List<MemberRelation> memberRelations = interfaceId2Relations.get(record.getId());
            if(memberRelations != null){
                for (MemberRelation memberRelation : memberRelations) {
                    members.add(memberRelation.getUserCode());
                }
            }
            record.setUserCode(members.stream().collect(Collectors.joining(",")));
        }
    }
    private Map<Long, List<MemberRelation>> getInterfaceRelations(List<InterfaceManage> interfaces){
        if(ObjectHelper.isEmpty(interfaces)) return Collections.emptyMap();
        final List<Long> interfaceIds = interfaces.stream().map(vs -> vs.getId()).collect(Collectors.toList());
        LambdaQueryWrapper<MemberRelation> lqw = new LambdaQueryWrapper<>();
        lqw.in(MemberRelation::getResourceId, interfaceIds);
        lqw.eq(MemberRelation::getResourceType, ResourceTypeEnum.INTERFACE.getCode());
        lqw.eq(MemberRelation::getYn, DataYnEnum.VALID.getCode());
        List<MemberRelation> relations = memberRelationService.list(lqw);
        final Map<Long, List<MemberRelation>> ret = relations.stream().collect(Collectors.groupingBy(MemberRelation::getResourceId));
        return ret;
    }
    public Page<MethodOutDto> getMethods(Long created, Long current, Long pageSize, Integer type){ //,Long modified
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MethodManage::getType,type);
        if(created != null){
            lqw.and(new Consumer<LambdaQueryWrapper<MethodManage>>() {
                @Override
                public void accept(LambdaQueryWrapper<MethodManage> wrapper) {
                    wrapper.or(wrapper1 -> {
                        wrapper1.ge( MethodManage::getCreated,new Date(created));

                    }).or(wrapper1 -> {
                        wrapper1.ge(MethodManage::getModified,new Date(created));
                    });

                }
            })/*.or(new Consumer<LambdaQueryWrapper<MethodManage>>() {
                @Override
                public void accept(LambdaQueryWrapper<MethodManage> wrapper) {
                    wrapper.ge(MethodManage::getModified,new Date(created));
                }
            })*/;
        }
        methodManageService.excludeBigTextFiled(lqw);
        Page<MethodManage> page = methodManageService.page(new Page<>(current, pageSize), lqw);


        Set<Long> interfaceIds = page.getRecords().stream().filter(item -> item.getInterfaceId() != null).map(item->item.getInterfaceId()).collect(Collectors.toSet());
        List<InterfaceManage> interfaceManages = interfaceManageService.listInterfaceByIds(new ArrayList(interfaceIds));
        Map<Long, Long> interfaceId2AppId = interfaceManages.stream().filter(item->item.getAppId()!=null).collect(Collectors.toMap(InterfaceManage::getId, InterfaceManage::getAppId));

        Collection<Long> appIds = interfaceId2AppId.values();
        List<AppInfo> appInfos = appInfoService.listByIds(appIds);
        Map<Long, List<AppInfo>> appId2Apps = appInfos.stream().collect(Collectors.groupingBy(AppInfo::getId));
        Map<Long, List<InterfaceManage>> interfaceIdMap = interfaceManages.stream().collect(Collectors.groupingBy(InterfaceManage::getId));

        Page<MethodOutDto> dto = new Page<>(page.getCurrent(),page.getSize(),page.getTotal());
        List<MethodOutDto> records = page.getRecords().stream().map(item->{
            MethodOutDto ret = new MethodOutDto();
            BeanUtils.copyProperties(item,ret);
            ret.setHttpMethod(EasyMockRemoteService.getHttpMethod(item.getHttpMethod()));
            ret.setHttpMethods(item.getHttpMethod());

            Long appId = interfaceId2AppId.get(item.getInterfaceId());

            ret.setDocUrl(String.format(METHOD_DOC_URL_TEMPLATE,appId,item.getId()));
            ret.setAppId(appId);
            List<AppInfo> appList = appId2Apps.get(appId);
            if(appList != null){
                ret.setAppCode(appList.get(0).getAppCode());
                ret.setJdosAppCode(appList.get(0).getJdosAppCode());

            }
            List<InterfaceManage> interfaces = interfaceIdMap.get(item.getInterfaceId());
            if(interfaces != null){
                ret.setDept(interfaces.get(0).getDeptName());
            }

            return ret;
        }).collect(Collectors.toList());
        dto.setRecords(records);
        return dto;
    }

}
