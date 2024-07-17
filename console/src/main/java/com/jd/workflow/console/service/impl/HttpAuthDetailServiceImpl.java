package com.jd.workflow.console.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.official.omdm.is.hr.vo.UserVo;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.base.enums.SiteEnum;
import com.jd.workflow.console.dao.mapper.HttpAuthDetailMapper;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.dto.doc.GroupSortModel;
import com.jd.workflow.console.dto.doc.InterfaceAppSortModel;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import com.jd.workflow.console.dto.doc.TreeSortModel;
import com.jd.workflow.console.entity.HttpAuthDetail;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.method.MethodModifyDeltaInfo;
import com.jd.workflow.console.helper.UserHelper;
import com.jd.workflow.console.service.IHttpAuthDetailService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IInterfaceMethodGroupService;
import com.jd.workflow.console.service.method.MethodModifyDeltaInfoService;
import com.jd.workflow.console.utils.NumberUtils;
import com.jd.workflow.console.utils.UserUtils;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 项目名称：parent
 *
 * @author wangwenguang
 * @date 2023-01-06 11:07
 */
@Service
@Slf4j
public class HttpAuthDetailServiceImpl extends ServiceImpl<HttpAuthDetailMapper, HttpAuthDetail> implements IHttpAuthDetailService {
    @Autowired
    ScheduledThreadPoolExecutor defaultScheduledExecutor;
    /**
     * 鉴权明细管理
     */
    @Resource
    private HttpAuthDetailMapper httpAuthDetailMapper;
    @Autowired
    private IInterfaceManageService interfaceManageService;


    @Resource
    private IInterfaceMethodGroupService methodGroupService;
    /**
     * 补充用户名称
     */
    @Autowired
    private UserHelper userHelper;
    @Autowired
    MethodModifyDeltaInfoService deltaInfoService;

    /**
     * 新增app
     *
     * @param queryDTO
     * @return
     */
    @Override
    public Page<HttpAuthDetailDTO> queryList(QueryHttpAuthDetailReqDTO queryDTO) {
        //分页处理
        Page<HttpAuthDetailDTO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getPageSize());
        long total = NumberUtils.toLong(httpAuthDetailMapper.queryListCount(queryDTO));
        page.setTotal(total);

        if (total > 0) {
            List<HttpAuthDetail> records = httpAuthDetailMapper.queryList(queryDTO);
            //转化为DTO
            List<HttpAuthDetailDTO> recordDTOs = toDTOList(records);
            setOwnerInfo(recordDTOs);
            page.setRecords(recordDTOs);
        }

        return page;
    }

    /**
     * 查询列表
     *
     * @param queryDTO
     * @return
     */
    @Override
    public List<HttpAuthDetailDTO> queryAllList(QueryHttpAuthDetailReqDTO queryDTO) {
        List<HttpAuthDetail> records = httpAuthDetailMapper.queryAllList(queryDTO);
        List<HttpAuthDetailDTO> recordDTOs = toDTOList(records);
        setOwnerInfo(recordDTOs);
        return recordDTOs;
    }

    /**
     * 查询列表
     *
     * @param queryDTO
     * @return
     */
    @Override
    public List<HttpAuthDetail> queryAllSourceList(QueryHttpAuthDetailReqDTO queryDTO) {
        return httpAuthDetailMapper.queryAllList(queryDTO);
    }

    /**
     * 转化为DTO列表
     * @param records
     * @return
     */
    private List<HttpAuthDetailDTO> toDTOList(List<HttpAuthDetail> records) {
        //转化为DTO
        return Optional.ofNullable(records).orElse(new ArrayList<>()).stream().map(v -> {
            return toDTO(v);
        }).collect(Collectors.toList());
    }

    /**
     * 查询列表数量
     *
     * @param queryDTO
     * @return
     */
    @Override
    public Long queryListCount(QueryHttpAuthDetailReqDTO queryDTO) {
        return httpAuthDetailMapper.queryListCount(queryDTO);
    }

    /**
     * 按接口分组展示
     *
     * @param queryDTO
     * @return
     */
    @Override
    public Page<HttpAuthDetailDTO> queryListPageGroupByInterface(QueryHttpAuthDetailReqDTO queryDTO) {
        Page<HttpAuthDetailDTO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getPageSize());
        long total = NumberUtils.toLong(httpAuthDetailMapper.queryCountPageGroupByInterface(queryDTO));
        page.setTotal(total);

        if (total > 0) {
            List<HttpAuthDetail> records = httpAuthDetailMapper.queryListPageGroupByInterface(queryDTO);
            List<HttpAuthDetailDTO> recordDTOs = toDTOList(records); //转化为DTO
            setOwnerInfo(recordDTOs);
            page.setRecords(recordDTOs);
        }
        return page;
    }

    /**
     * 按接口分组展示
     *
     * @param queryDTO
     * @return
     */
    @Override
    public List<HttpAuthDetailDTO> queryListGroupByInterface(QueryHttpAuthDetailReqDTO queryDTO) {
        List<HttpAuthDetail> records = httpAuthDetailMapper.queryListGroupByInterface(queryDTO);
        List<HttpAuthDetailDTO> recordDTOs = toDTOList(records);
        setOwnerInfo(recordDTOs);
        return recordDTOs;
    }

    /**
     * 设置应用负责人信息
     *
     * @param recordDTOs
     */
    private void setOwnerInfo(List<HttpAuthDetailDTO> recordDTOs) {
        if (CollectionUtils.isEmpty(recordDTOs)) {
            return;
        }
        Map<String, String> ownerMap = new HashMap<>();
        for (HttpAuthDetailDTO authDetailDTO : recordDTOs) {
            List<String> ownerList = UserUtils.getOwners(authDetailDTO.getMembers());
            if (CollectionUtils.isNotEmpty(ownerList)) {
                String ownerErp = ownerList.get(0);
                String ownerName = ownerMap.get(ownerErp);
                if (StringUtils.isNotBlank(ownerName)) {
                    authDetailDTO.setOwnerErp(ownerErp);
                    authDetailDTO.setOwnerName(ownerName);
                    continue;
                }
                UserVo userVo = userHelper.getUserBaseInfoByUserName(ownerList.get(0));
                if (userVo != null) {
                    authDetailDTO.setOwnerErp(ownerErp);
                    authDetailDTO.setOwnerName(userVo.getRealName());
                    ownerMap.put(ownerErp,userVo.getRealName());
                }
            }
        }
    }



    /**
     * 按方法分组展示
     *
     * @param queryDTO
     * @return
     */
    @Override
    public List<HttpAuthDetailDTO> queryListGroupByMethod(QueryHttpAuthDetailReqDTO queryDTO) {
        List<HttpAuthDetail> records = httpAuthDetailMapper.queryListGroupByMethod(queryDTO);
        List<HttpAuthDetailDTO> recordDTOs = toDTOList(records);
        setOwnerInfo(recordDTOs);
        return recordDTOs;
    }

    /**
     * 按方法分组展示
     *
     * @param queryDTO
     * @return
     */
    @Override
    public Page<HttpAuthDetailDTO> queryListPageGroupByMethod(QueryHttpAuthDetailReqDTO queryDTO) {
        Page<HttpAuthDetailDTO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getPageSize());
        long total = NumberUtils.toLong(httpAuthDetailMapper.queryCountPageGroupByMethod(queryDTO));
        page.setTotal(total);

        if (total > 0) {
            List<HttpAuthDetail> records = httpAuthDetailMapper.queryListPageGroupByMethod(queryDTO);
            initMethodDeltaInfos(records);
            List<HttpAuthDetailDTO> recordDTOs = toDTOList(records); //转化为DTO
            setOwnerInfo(recordDTOs);
            page.setRecords(recordDTOs);
        }
        return page;
    }
    public void initMethodDeltaInfos(List<HttpAuthDetail> methods){
        if(ObjectHelper.isEmpty(methods)) return;
        List<Long> ids = methods.stream().map(item->item.getMethodId()).collect(Collectors.toList());
        List<MethodModifyDeltaInfo> deltas = deltaInfoService.getMethodDeltas(ids);
        final Map<Long, List<HttpAuthDetail>> id2Methods = methods.stream().collect(Collectors.groupingBy(HttpAuthDetail::getMethodId));
        for (MethodModifyDeltaInfo delta : deltas) {
            List<HttpAuthDetail> found = id2Methods.get(delta.getMethodId());
            if(found == null) continue;
            Map deltaContent = JsonUtils.parse(delta.getDeltaContent(), Map.class);
            Map<String,Object> deltaAttrs = (Map<String, Object>) deltaContent.get("deltaAttrs");
            if(!ObjectHelper.isEmpty(deltaAttrs)){
                String name = (String) deltaAttrs.get("name");
                if(StringUtils.isNotBlank(name)){
                    found.get(0).setMethodName(name);
                }
            }

        }
    }

    /**
     * 按鉴权分组展示
     *
     * @param queryDTO
     * @return
     */
    @Override
    public List<HttpAuthDetailDTO> queryListGroupByAuthCode(QueryHttpAuthDetailReqDTO queryDTO) {
        List<HttpAuthDetail> records = httpAuthDetailMapper.queryListGroupByAuthCode(queryDTO);
        List<HttpAuthDetailDTO> recordDTOs = toDTOList(records);
        return recordDTOs;
    }

    /**
     * 查询列表
     *
     * @param authDetailDTOS
     * @return
     */
    @Override
    public boolean saveBatch(List<HttpAuthDetailDTO> authDetailDTOS) {
        if (CollectionUtils.isEmpty(authDetailDTOS)){
            return false;
        }
        List<HttpAuthDetail> authDetails = toEntityList(authDetailDTOS);
        log.info("#HttpAuthDetailServiceImpl.saveBatch.request= {}", JSON.toJSONString(authDetails));
        boolean success =  saveBatch(authDetails, 50);
        log.info("#HttpAuthDetailServiceImpl.saveBatch.result= {}",success);
        return success;
    }

    /**
     * 批量删除
     *
     * @param authDetails
     * @return
     */
    @Override
    public boolean removeBatch(List<HttpAuthDetail> authDetails) {
        if (CollectionUtils.isEmpty(authDetails)){
            return true;
        }
        List<Long> idList = toIdList(authDetails);
        log.info("#HttpAuthDetailServiceImpl.removeBatch= {}",idList);
        boolean success = removeByIds(idList);
        log.info("#HttpAuthDetailServiceImpl.result= {}",success);
        return success;
    }

    @Override
    public void removeInterfaceAuthDetail(Long interfaceId) {
        LambdaQueryWrapper<HttpAuthDetail> lqw = new LambdaQueryWrapper();
        lqw.eq(HttpAuthDetail::getInterfaceId,interfaceId);
        remove(lqw);
    }

    @Override
    public void removeMethodAuthDetail(List<Long> methodIds) {
        if(methodIds.isEmpty()) return;
        LambdaQueryWrapper<HttpAuthDetail> lqw = new LambdaQueryWrapper();
        lqw.in(HttpAuthDetail::getMethodId,methodIds);
        remove(lqw);
    }

    @Override
    public Set<Long> queryExists(List<Long> interfaceIds) {
        if(interfaceIds.isEmpty()) return Collections.emptySet();
        QueryWrapper<HttpAuthDetail> lqw = new QueryWrapper();
        lqw.in("interface_id",interfaceIds);
        lqw.select("distinct interface_id as interfaceId");
        List<Map<String, Object>> httpAuthDetails = listMaps(lqw);
        Set<Long> result = new HashSet<>();
        for (Map<String, Object> map : httpAuthDetails) {
            result.add(Variant.valueOf(map.get("interfaceId")).toLong());
        }
        return result;
    }

    @Override
    public List<HttpAuthDetailDTO> queryAppInterfaceAuth(QueryHttpAuthDetailReqDTO query) {
        Guard.notNull(query, "查询接口维度鉴权标识明细列表时，入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(query.getSite()), "查询接口维度鉴权标识明细列表时，site 参数不正确！");
        query.setPin(UserSessionLocal.getUser().getUserId());
        if (Objects.isNull(query.getType())) {  // 查询http鉴权接口
            List<HttpAuthDetailDTO> httpAuthList = queryListGroupByInterface(query);
            log.info("#HttpAuthDetailController queryListGroupByInterface result={} ", JSON.toJSONString(httpAuthList));
            return httpAuthList;

        } else if (Objects.equals(query.getType(), 1)) {
            InterfacePageQuery interfacePageQuery = new InterfacePageQuery();
            // 查询HTTP、JSF接口
            interfacePageQuery.setType(InterfaceTypeEnum.HTTP.getCode()+","+InterfaceTypeEnum.JSF.getCode()+","+InterfaceTypeEnum.EXTENSION_POINT.getCode());
            interfacePageQuery.setCurrent(query.getCurrent());
            //interfacePageQuery.setSize(Objects.isNull(query.getPageSize()) ? 0 : Long.valueOf(query.getPageSize()));
            interfacePageQuery.setSize(200L);
            interfacePageQuery.setAppId(org.apache.commons.lang3.math.NumberUtils.toLong(query.getAppCode(), -1));
            interfacePageQuery.setTenantId(UserSessionLocal.getUser().getTenantId());
            interfacePageQuery.setName(query.getInterfaceInfo());
            interfacePageQuery.setErp("org.lht");//demo用户查询，走默认账号权限。
            //3.service层
            Page<InterfaceManage> interfaceManagePage = interfaceManageService.pageList(interfacePageQuery);
            for (InterfaceManage record : interfaceManagePage.getRecords()) {
                record.init();
            }
            List<HttpAuthDetailDTO> collect = interfaceManagePage.getRecords().stream().map(e -> {
                //MethodGroupTreeDTO methodGroupTree = methodGroupService.findMethodGroupTree(e.getId());
                HttpAuthDetailDTO httpDto = new HttpAuthDetailDTO();
                httpDto.setId(e.getId());
                httpDto.setInterfaceType(e.getType());
                httpDto.setAppName(e.getAppName());
                httpDto.setAppCode(e.getAppId() + "");
                httpDto.setModifier(e.getModifier());
                httpDto.setCreator(e.getUserName());
                httpDto.setInterfaceId(e.getId());
                if(InterfaceTypeEnum.HTTP.getCode().equals(e.getType() )){
                    httpDto.setName(e.getName()+"("+e.getServiceCode()+")");
                }else{
                    httpDto.setName(e.getName()+"("+e.getServiceCode()+")");
                }
                httpDto.setInterfaceName(e.getName());
                httpDto.setInterfaceCode(e.getServiceCode());
                // 接口/应用文件夹类型
                httpDto.setType(Integer.valueOf(TreeSortModel.TYPE_INTERFACE));
                //httpDto.setChildren(Objects.isNull(methodGroupTree.getTreeModel()) ? null : methodGroupTree.getTreeModel().getTreeItems());
                return httpDto;
            }).collect(Collectors.toList());
            //4.出参
            return collect;
        }
        throw new BizException("无效的查询参数！");

    }

    @Override
    public List<HasChildrenHttpAuthDetail> queryInterfaceMethod(QueryHttpAuthDetailReqDTO query) {
        Guard.notNull(query, "查询方法维度鉴权标识明细列表时，入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(query.getSite()), "查询方法维度鉴权标识明细列表时，site 参数不正确！");
        query.setPin(UserSessionLocal.getUser().getUserId());
        if (Objects.isNull(query.getType())) {// 查询接口下的方法
            List<HttpAuthDetailDTO> httpAuthList = queryListGroupByMethod(query);
            log.info("#HttpAuthApplyDetailController queryListGroupByMethod result={} ", JSON.toJSONString(httpAuthList));
            return httpAuthList.stream().map(item->HasChildrenHttpAuthDetail.from(item)).collect(Collectors.toList());
        } else if (Objects.equals(query.getType(), 1)) { // 查询应用下的接口以及方法
            List<HasChildrenHttpAuthDetail> httpAuthList = new ArrayList<>();
            MethodGroupTreeDTO methodGroupTree = methodGroupService.findMethodGroupTree(query.getInterfaceId());
            //查询接口信息
            InterfaceManage interfaceManage = interfaceManageService.getById(query.getInterfaceId());
            MethodGroupTreeModel sortGroupTree = methodGroupTree.getTreeModel();
            List<TreeSortModel> treeItems = sortGroupTree.getTreeItems();
            treeItems.forEach(e -> httpAuthList.add(getMethodAndGroups(e, interfaceManage)));
            return httpAuthList;
        }
        return null;
    }

    private HasChildrenHttpAuthDetail getMethodAndGroups(TreeSortModel treeItem, InterfaceManage interfaceManage) {
        HasChildrenHttpAuthDetail dto = new HasChildrenHttpAuthDetail();
        dto.setAppName(interfaceManage.getAppName());
        dto.setAppCode(interfaceManage.getAppId() + "");
        dto.setModifier(interfaceManage.getModifier());
        dto.setCreator(interfaceManage.getCreator());
        dto.setInterfaceId(interfaceManage.getId());
        dto.setInterfaceName(interfaceManage.getName());
        if (Objects.isNull(treeItem)) {
            return dto;
        }
        dto.setId(treeItem.getId());
        dto.setMethodName(treeItem.getName());
        dto.setMethodCode(treeItem.getEnName());
        dto.setMethodId(treeItem.getId());
        dto.setPath(treeItem.getPath());
        dto.setName(treeItem.getName());
        dto.setInterfaceType(interfaceManage.getType());
        if (treeItem instanceof MethodSortModel) {
            dto.setType(1);

            return dto;
        } else if (treeItem instanceof GroupSortModel) {
            List<HasChildrenHttpAuthDetail> childrenList = new ArrayList<>();
            dto.setType(2);
            List<TreeSortModel> children = ((GroupSortModel) treeItem).getChildren();
            if (org.apache.commons.collections4.CollectionUtils.isEmpty(children)) {
                return dto;
            }
            children.forEach(e -> {
                childrenList.add(getMethodAndGroups(e, interfaceManage));
            });
            dto.setChildren(childrenList);
        }
        return dto;
    }

    @Override
    public List<HasChildrenHttpAuthDetail> queryAppInterfaceAndMethod(QueryHttpAuthDetailReqDTO query) {
        List<HttpAuthDetailDTO> httpAuthDetailDTOS = queryAppInterfaceAuth(query);
        List<Future> futures = new ArrayList<>();
        List<HasChildrenHttpAuthDetail> result = new ArrayList<>();
        for (HttpAuthDetailDTO httpAuthDetailDTO : httpAuthDetailDTOS) {
            QueryHttpAuthDetailReqDTO newQuery = query.clone();
            newQuery.setInterfaceId(httpAuthDetailDTO.getInterfaceId());
            HasChildrenHttpAuthDetail detailItem = HasChildrenHttpAuthDetail.from(httpAuthDetailDTO);
            result.add(detailItem);
            futures.add(defaultScheduledExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try{
                        List<HasChildrenHttpAuthDetail> dtos = queryInterfaceMethod(newQuery);
                        detailItem.setChildren(dtos);
                    }catch (Exception e){
                        log.error("app.err_query_interface_method:query={}",query,e);
                    }
                }
            }));
        }
        for (Future future : futures) {
            try {
                future.get();
            } catch (Exception e) {
              throw new BizException("查询失败",e);
            }
        }
        return result;
    }

    /**
     * 转化为DTO
     *
     * @param authDetail
     * @return
     */
    private HttpAuthDetailDTO toDTO(HttpAuthDetail authDetail) {
        if (authDetail == null) {
            return null;
        }
        HttpAuthDetailDTO authDetailDTO = new HttpAuthDetailDTO();
        BeanUtils.copyProperties(authDetail, authDetailDTO);
        return authDetailDTO;
    }

    /**
     * 转化为DTO
     *
     * @param authDetailDTO
     * @return
     */
    private HttpAuthDetail toEntity(HttpAuthDetailDTO authDetailDTO) {
        if (authDetailDTO == null) {
            return null;
        }
        HttpAuthDetail authDetail = new HttpAuthDetail();
        BeanUtils.copyProperties(authDetailDTO, authDetail);
        return authDetail;
    }

    /**
     * 转化为实体列表
     * @param records
     * @return
     */
    private List<HttpAuthDetail> toEntityList(List<HttpAuthDetailDTO> records) {
        //转化为DTO
        return Optional.ofNullable(records).orElse(new ArrayList<>()).stream().map(v -> {
            return toEntity(v);
        }).collect(Collectors.toList());
    }

    /**
     * 转化为ID列表
     * @param records
     * @return
     */
    private List<Long> toIdList(List<HttpAuthDetail> records) {
        //转化为DTO
        return Optional.ofNullable(records).orElse(new ArrayList<>()).stream().map(v -> {
            return v.getId();
        }).collect(Collectors.toList());
    }
}
