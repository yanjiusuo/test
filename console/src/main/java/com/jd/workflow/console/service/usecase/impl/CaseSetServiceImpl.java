package com.jd.workflow.console.service.usecase.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.CaseTypeEnum;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dao.mapper.InterfaceManageMapper;
import com.jd.workflow.console.dao.mapper.group.RequirementInterfaceGroupMapper;
import com.jd.workflow.console.dao.mapper.usecase.CaseSetMapper;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.doc.GroupSortModel;
import com.jd.workflow.console.dto.doc.TreeSortModel;
import com.jd.workflow.console.dto.usecase.*;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MemberRelation;
import com.jd.workflow.console.entity.param.ParamBuilder;
import com.jd.workflow.console.entity.requirement.RequirementInterfaceGroup;
import com.jd.workflow.console.entity.usecase.CaseSet;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.group.RequirementInterfaceGroupService;
import com.jd.workflow.console.service.impl.AppInfoServiceImpl;
import com.jd.workflow.console.service.param.IParamBuilderService;
import com.jd.workflow.console.service.requirement.RequirementInfoService;
import com.jd.workflow.console.service.role.AccRoleServiceAdapter;
import com.jd.workflow.console.service.role.AdminHelper;
import com.jd.workflow.console.service.usecase.CaseSetManager;
import com.jd.workflow.console.service.usecase.CaseSetService;
import com.jd.workflow.console.utils.NumberUtils;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @description: 用例集表 服务实现类
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
@Service
@Slf4j
public class CaseSetServiceImpl extends ServiceImpl<CaseSetMapper, CaseSet> implements CaseSetService {

    @Resource
    private CaseSetMapper caseSetMapper;
    @Resource
    private InterfaceManageMapper interfaceManageMapper;
    @Resource
    private RequirementInterfaceGroupMapper requirementInterfaceGroupMapper;
    @Resource
    private RequirementInfoService requirementInfoService;
    @Autowired
    private AccRoleServiceAdapter accRoleServiceAdapter;
    @Resource
    private RequirementInterfaceGroupService requirementInterfaceGroupService;
    @Autowired
    private IParamBuilderService paramBuilderService;
    @Autowired
    private CaseSetManager caseSetManager;
    @Resource
    private AppInfoServiceImpl appInfoService;
    @Autowired
    private IInterfaceManageService interfaceManageService;
    @Autowired
    private AdminHelper adminHelper;

    @Autowired
    private IMethodManageService methodManageService;

    private static final int statusJsf=0;
    private static final int statusHttp=1;
    private static final int statusJsfHttp=2;


    @Override
    public IPage<CaseSetDTO> pageList(Long current, Long pageSize, String name, Long requirementId) {
        current = Objects.isNull(current) || current < 0 ? 0 : current;
        pageSize = Objects.isNull(pageSize) || pageSize < 0 ? 10 : pageSize;
        Page page = new Page(current, pageSize);
        Page<CaseSetDTO> pageResult = caseSetMapper.pageList(page, name, requirementId);
        return pageResult;
    }

    @Override
    public RequiremenUnderInterfacesDTO  getRequiremenUnderInterfaces(Long requirementId, Long appId) {
        RequiremenUnderInterfacesDTO requiremenUnderInterfacesDTO = new RequiremenUnderInterfacesDTO();
        List<TreeItem> resJSF=getRequiremenUnderInterfacesType(requirementId,appId,InterfaceTypeEnum.JSF.getCode());
        List<TreeItem> resHTTP=getRequiremenUnderInterfacesType(requirementId,appId,InterfaceTypeEnum.HTTP.getCode());
        requiremenUnderInterfacesDTO.setJSFCases(resJSF);
        requiremenUnderInterfacesDTO.setHTTPCases(resHTTP);
        return requiremenUnderInterfacesDTO;
    }


    public List<TreeItem> getRequiremenUnderInterfacesType(Long requirementId, Long appId,Integer type) {
        List<TreeItem> retData = new ArrayList<>();
        // 查询应用下对应type的接口
        List<CaseInterfaceManageDTO> interfaceManageDTOList = interfaceManageMapper.selectByAppId(appId, type);
        if (CollectionUtils.isEmpty(interfaceManageDTOList)) {
            return retData;
        }

        // 对应的所有 group 数据，但需要遍历取 json 串中 type = 1 的数据才是接口
        List<RequirementInterfaceGroup> interfaceGroupList = requirementInterfaceGroupService.getInterfaces(requirementId, type);
        Map<Long, List<RequirementInterfaceGroup>> longListMap = interfaceGroupList.stream().collect(Collectors.groupingBy(RequirementInterfaceGroup::getInterfaceId));

        interfaceManageDTOList.forEach(interfaceManageDTO -> {

            // 第一级节点数据补充
            interfaceManageDTO.setKey(interfaceManageDTO.getId().toString());
            Map<Long, TreeItem> methodMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(longListMap.get(interfaceManageDTO.getId()))) {
                TreeItem interfaceItem = new TreeItem(interfaceManageDTO.getServiceCode(),String.valueOf(interfaceManageDTO.getId()),1);
                // 遍历 app 下的所有分组数据，有接口、有文件夹，具体见 TreeSortModel
                longListMap.get(interfaceManageDTO.getId()).forEach(dbGroup -> {
                    if (dbGroup.getSortGroupTree() != null && CollectionUtils.isNotEmpty(dbGroup.getSortGroupTree().getTreeItems())) {
                        dbGroup.getSortGroupTree().getTreeItems().forEach((TreeSortModel treeItemGroup) -> {
                            // type = 1 方法 2-分组
                            if (Objects.equals(treeItemGroup.getType(), "2") && treeItemGroup instanceof GroupSortModel) {
                                GroupSortModel groupSortModel = (GroupSortModel) treeItemGroup;
                                if (CollectionUtils.isNotEmpty(groupSortModel.getChildren())) {
                                    for (TreeSortModel treeSortModel : groupSortModel.getChildren()) {
                                        if (Objects.equals(treeSortModel.getType(), "1")) {
                                            String showName;
                                            if(StringUtils.isNotBlank(treeSortModel.getName())){
                                                showName=treeSortModel.getName();
                                            }else if(StringUtils.isNotBlank(treeSortModel.getEnName())){
                                                showName=treeSortModel.getEnName();
                                            }else{
                                                showName=methodManageService.getById(treeSortModel.getId()).getMethodCode();
                                            }
                                            TreeItem methodItem = new TreeItem(showName, interfaceManageDTO.getId()+"-"+treeSortModel.getId(), 2);
                                            interfaceItem.getChildren().add(methodItem);
                                            methodMap.put(treeSortModel.getId(), methodItem);
                                        }
                                    }
                                }
                            }
                        });
                    }
                });

                // 用例列表
                if (MapUtils.isNotEmpty(methodMap)) {
                    // 根据方法获取用例列表
                    List<ParamBuilder> paramBuilders = paramBuilderService.listByMethodIds(StringUtils.join(methodMap.keySet(), ","));
                    if (CollectionUtils.isNotEmpty(paramBuilders)) {
                        Map<Long, List<ParamBuilder>> paramBuilderMap = paramBuilders.stream().collect(Collectors.groupingBy(ParamBuilder::getMethodManageId));
                        methodMap.entrySet().forEach(entry -> {
                            if (CollectionUtils.isNotEmpty(paramBuilderMap.get(entry.getKey()))) {
                                entry.getValue().setChildren(paramBuilderMap.get(entry.getKey()).stream().map(row -> {
                                    TreeItem caseItem = new TreeItem(row.getSceneName(), entry.getValue().getKey()+"-"+row.getId(), 3);
                                    return caseItem;
                                }).collect(Collectors.toList()));
                            }
                        });
                    }
                }
                retData.add(interfaceItem);
            }
        });
        return retData;
    }

    @Override
    public boolean checkAuth(Long requirementId, String userId) {
        Boolean retBol = Boolean.FALSE;
        if (adminHelper.isNormalAdmin()) {
            retBol= Boolean.TRUE;
        }else{
            List<MemberRelation> members = requirementInfoService.getMembers(requirementId);
            members = members == null ? Lists.newArrayList() : members;
            List<String> erpList = members.stream().map(MemberRelation::getUserCode).collect(Collectors.toList());
            if (erpList.contains(UserSessionLocal.getUser().getUserId()) ) {
                retBol= Boolean.TRUE;
            }
        }
        return retBol;
    }

    @Override
    public void add(CaseSet caseSet){
        CaseType casejson = JSON.parseObject(caseSet.getSelectedData(), CaseType.class);
        List<String> httpSelectedList = casejson.getHttp().getSelected();
        List<String> jsfSelectedList = casejson.getJsf().getSelected();

        addCheckParam(caseSet,httpSelectedList,jsfSelectedList);
        //编辑
        if(Objects.nonNull(caseSet.getId())){
            caseSet.setCreator(null);
            caseSet.setName(null);
            caseSet.setExeCount(null);
            caseSet.setModifier(UserSessionLocal.getUser().getUserId());
        }else{
            List<CaseSet> caseSets = queryByNameAndRequirementId(caseSet.getName(), caseSet.getRequirementId());
            if(CollectionUtils.isNotEmpty(caseSets)){
                throw new BizException("同一个需求下的任务名称不能重复");
            }
            caseSet.setCreator(UserSessionLocal.getUser().getUserId());
        }

        if(!httpSelectedList.isEmpty() && !jsfSelectedList.isEmpty()){
            caseSet.setCaseType(CaseTypeEnum.caseTypeJsfHttp.getCode());
        }else if(!httpSelectedList.isEmpty()){
            caseSet.setCaseType(CaseTypeEnum.caseTypeHttp.getCode());
        }else{
            caseSet.setCaseType(CaseTypeEnum.caseTypeJsf.getCode());
        }
        saveOrUpdate(caseSet);
    }

    /**
     * 保存数据时数据校验
     * @param caseSet
     */
    private void addCheckParam(CaseSet caseSet,List<String> httpSelectedList,List<String> jsfSelectedList) {
        Guard.notNull(caseSet.getName(), "名称不能为null");
        Guard.notNull(caseSet.getAppId(), "appId不能为null");
        Guard.notNull(caseSet.getRequirementId(), "需求空间不能为null");
        if(httpSelectedList.isEmpty() && jsfSelectedList.isEmpty()){
            Guard.notNull(true, "至少选择一条用例数据");
        }
        if(caseSet.getName().length()>20){
            Guard.notNull(true, "名称长度需在1~20位字符之前");
        }
        if (!checkAuth(caseSet.getRequirementId(), UserSessionLocal.getUser().getUserId())) {
            throw new BizException("无权查看");
        }
    }

    @Override
    public Boolean delById(Long id) {
        CaseSet caseSet = getById(id);
        if (Objects.isNull(caseSet)) {
            throw new BizException("没有此用例集【" + id + "】");
        }
        if (!checkAuth(caseSet.getRequirementId(), UserSessionLocal.getUser().getUserId())) {
            throw new BizException("无权查看");
        }
        return caseSetManager.delById(id);
    }

    @Override
    public CaseSetDTO detailById(Long id) {
        CaseSet caseSet = getById(id);
        if(Objects.nonNull(caseSet)){
            if (!checkAuth(caseSet.getRequirementId(), UserSessionLocal.getUser().getUserId())) {
                throw new BizException("无权查看");
            }
        }
        CaseSetDTO caseSetDTO = new CaseSetDTO();
        BeanUtils.copyProperties(caseSet,caseSetDTO);
        AppInfoDTO app = appInfoService.findApp(caseSetDTO.getAppId());
        if(Objects.nonNull(app)){
            caseSetDTO.setAppCode(app.getAppCode());
            caseSetDTO.setAppName(app.getAppName());
        }
        List<Long> interfaceIdList = obtainJsfInterfaceIdList(caseSet);
        List<InterfaceManage> interfaceManages = interfaceManageService.listInterfaceByIdsOnlyOne(interfaceIdList);
        if(CollectionUtils.isNotEmpty(interfaceManages)){
            caseSetDTO.setServiceCode(interfaceManages.get(0).getServiceCode());
        }
        return caseSetDTO;
    }

    @Override
    public CaseSet obtainById(Long id) {
        LambdaQueryWrapper<CaseSet> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(CaseSet::getId, id);
        queryWrapper.eq(CaseSet::getYn, DataYnEnum.VALID.getCode());
        return getOne(queryWrapper);
    }

    /**
     * 获取接口数据
     * @param caseSet
     * @return
     */
    private List<Long> obtainInterfaceIdList(CaseSet caseSet) {
//        List<String> caseIdList = JSON.parseObject(caseSet.getSelectedData(), new TypeReference<ArrayList<String>>(){});
        CaseType casejson = JSON.parseObject(caseSet.getSelectedData(), CaseType.class);
        List<String> httpSelectedList = casejson.getHttp().getSelected();
        List<String> jsfSelectedList = casejson.getJsf().getSelected();
        List<String> caseIdList = Stream.concat(httpSelectedList.stream(), jsfSelectedList.stream())
                .collect(Collectors.toList());
        if(Objects.isNull(caseIdList)){
            caseIdList = new ArrayList<>();
        }
        List<Long> InterfaceIds = caseIdList.stream().map(data -> {
            if (StringUtils.isNotBlank(data)) {
                String[] split = data.split("-");
                if (split.length == 3) {
                    return NumberUtils.toLong(split[0]);
                }
            }
            return null;
        }).filter(data -> {
            return Objects.nonNull(data);
        }).collect(Collectors.toList());
        return InterfaceIds;
    }

    /**
     * 获取JSF的用例集
     * @param caseSet
     * @return
     */
    private List<Long> obtainJsfInterfaceIdList(CaseSet caseSet) {
        CaseType caseJson = JSON.parseObject(caseSet.getSelectedData(), CaseType.class);
        List<String> jsfSelectedList = caseJson.getJsf().getSelected();
        List<Long> interfaceIds = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(jsfSelectedList)){
            interfaceIds = jsfSelectedList.stream().map(data -> {
                if (StringUtils.isNotBlank(data)) {
                    String[] split = data.split("-");
                    if (split.length == 3) {
                        return NumberUtils.toLong(split[0]);
                    }
                }
                return null;
            }).filter(data -> {
                return Objects.nonNull(data);
            }).collect(Collectors.toList());
        }
        return interfaceIds;
    }

    /**
     * 通过 name和requirementId 查询用例集数据
     * @param name
     * @param requirementId
     * @return
     */
    public List<CaseSet> queryByNameAndRequirementId(String name , Long requirementId){
        LambdaQueryWrapper<CaseSet> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(CaseSet::getRequirementId, requirementId)
                .eq(CaseSet::getName,name)
                .eq(CaseSet::getYn, DataYnEnum.VALID.getCode());

        return list(queryWrapper);
    }
}
