package com.jd.workflow.console.rpc;

import com.alibaba.excel.util.IoUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jd.common.util.StringUtils;
import com.jd.matrix.metadata.trace.report.utils.JsfUtils;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.dto.doc.GroupSortModel;
import com.jd.workflow.console.dto.doc.InterfaceSortModel;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import com.jd.workflow.console.dto.doc.TreeSortModel;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.doc.InterfaceVersion;
import com.jd.workflow.console.entity.doc.MethodVersionModifyLog;
import com.jd.workflow.console.entity.requirement.RequirementInfo;
import com.jd.workflow.console.entity.requirement.RequirementInterfaceGroup;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.doc.IInterfaceVersionService;
import com.jd.workflow.console.service.doc.IMethodVersionModifyLogService;
import com.jd.workflow.console.service.group.RequirementInterfaceGroupService;
import com.jd.workflow.console.service.group.impl.RequirementGroupServiceImpl;
import com.jd.workflow.console.service.impl.InterfaceManageServiceImpl;
import com.jd.workflow.console.service.impl.InterfaceMethodGroupServiceImpl;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.console.service.requirement.*;
import com.jd.workflow.console.utils.JfsUtils;
import com.jd.workflow.export.DocExportService;
import com.jd.workflow.server.dto.*;
import com.jd.workflow.server.dto.tree.InterfaceTreeItem;
import com.jd.workflow.server.service.InterfaceGetRpcService;
import com.jd.workflow.soap.common.cache.ICache;
import com.jd.workflow.soap.common.cache.impl.MemoryCache;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StringHelper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InterfaceGetRpcServiceImpl implements InterfaceGetRpcService {
    static final String URL= "http://console.paas.jd.com/idt/fe-demand-view/requirement/";
    @Autowired
    RequirementInfoService requirementInfoService;
    private static String CACHE_KEY_PREFIX = "jsf:id:";
    @Autowired
    JfsUtils jfsUtils;
    @Autowired
    RequirementInterfaceGroupService requirementInterfaceGroupService;
    @Autowired
    MethodManageServiceImpl methodManageService;
    @Autowired
    InterfaceManageServiceImpl interfaceManageService;
    @Autowired
    IAppInfoService appInfoService;
    @Autowired
    IMethodVersionModifyLogService methodVersionModifyLogService;
    @Autowired
    DocExportService docExportService;
    @Autowired
    IInterfaceVersionService interfaceVersionService;
    @Autowired
    InterfaceMethodGroupServiceImpl interfaceMethodGroupService;

    @Autowired
    ScheduledThreadPoolExecutor defaultScheduledExecutor;

    ICache cache;

    @PostConstruct
    public void init(){
        cache = new MemoryCache();
    }

    @Override
    public QueryResult<List<JsfOrHttpInfo>> queryJsfOrHttp(int type, Long flowId) {
        log.info("query_http_itnerface:type={},flowId={}",type,flowId);
        if(flowId == null){
            return QueryResult.error("flowId不能为空");
        }
        if(type != 1 && type != 3){
            return QueryResult.error("type只能为1或者3");
        }
         RequirementInfo requirementInfo = requirementInfoService.getByFlowId(flowId);
        if(requirementInfo == null){
            return QueryResult.error("需求不存在");
        }
        try{
            if(InterfaceTypeEnum.HTTP.getCode().equals(type)){
                List<RequirementInterfaceGroup> entities = requirementInterfaceGroupService.getInterfaceGroups(requirementInfo.getId(), type, false);

                List<MethodSortModel> sortModels = new ArrayList<>();
                List<MethodSortModel> methods = entities.stream().map(item -> {
                    return item.getSortGroupTree().allMethods();
                }).filter(item -> {
                    return item != null;
                }).flatMap(item -> {
                    return item.stream();
                }).collect(Collectors.toList());
                List<MethodManage> methodManages = methodManageService.listMethods(methods.stream().map(item -> item.getId()).collect(Collectors.toList()));
                List<JsfOrHttpInfo> result = methodManages.stream().map(method -> {
                    JsfOrHttpInfo info = new JsfOrHttpInfo();
                    info.setId(method.getId());
                    info.setName(method.getName());
                    info.setHttpMethod(method.getHttpMethod());
                    info.setPath(method.getPath());
                    info.setUrl(URL + requirementInfo.getId());
                    return info;
                }).collect(Collectors.toList());
                return QueryResult.buildSuccessResult(result);
            }else{
                List<RequirementInterfaceGroup> entities = requirementInterfaceGroupService.getInterfaceGroups(requirementInfo.getId(), type, false);
                List<Long> ids  = entities.stream().map(item->item.getInterfaceId()).collect(Collectors.toList());
                List<InterfaceManage> interfaceManages = interfaceManageService.listInterfaceByIds(ids);
                List<JsfOrHttpInfo> result = interfaceManages.stream().map(item->{
                    JsfOrHttpInfo info = new JsfOrHttpInfo();
                    info.setId(item.getId());
                    info.setName(item.getServiceCode());
                    //info.setHttpMethod(method.getHttpMethod());
                    //info.setPath(method.getPath());
                    info.setUrl(URL + requirementInfo.getId());
                    return info;
                }).collect(Collectors.toList());
                return QueryResult.buildSuccessResult(result);
            }
        }catch (Exception e){
            e.printStackTrace();
            return QueryResult.error("请求失败");

        }

    }

    @Override
    public QueryResult<JsfDocInfo> queryJsfUrl(String jsfName) {
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceManage::getServiceCode,jsfName);
        lqw.eq(InterfaceManage::getType,InterfaceTypeEnum.JSF.getCode());
        lqw.eq(InterfaceManage::getYn,1);
        lqw.orderByDesc(InterfaceManage::getId);
        Page<InterfaceManage> page = interfaceManageService.page(new Page<>(1, 1), lqw);
        JsfDocInfo docInfo = new JsfDocInfo();
        Object objId = cache.hGet(CACHE_KEY_PREFIX, jsfName);
        if(objId != null ){
            return QueryResult.buildSuccessResult(buildJsfDocInfo(Long.parseLong(objId.toString())));
        }
        if(!page.getRecords().isEmpty()){
            Long id = page.getRecords().get(0).getId();
            cache.hSet(CACHE_KEY_PREFIX,jsfName,id,5*60);
            docInfo = buildJsfDocInfo(id);
        }

        return QueryResult.buildSuccessResult(docInfo);
    }

    @Override
    public QueryResult<List<JsfOrHttpInterfaceInfo>> queryJsfOrHttpInterface(int appType, String appCode) {
        try{
            Long appId = null;
            AppInfo appInfo;
            if(appType == 1) { // jdos
                appInfo = appInfoService.findByJdosCode(appCode);
            }else{
                appInfo = appInfoService.findApp(appCode);
            }
            if(appInfo == null){
                return QueryResult.buildSuccessResult(new ArrayList<>());
            }
            List<InterfaceManage> appInterfaces = interfaceManageService.getAppInterface(appInfo.getId());
            List<JsfOrHttpInterfaceInfo> list = appInterfaces.stream().map(item -> {
                JsfOrHttpInterfaceInfo info = new JsfOrHttpInterfaceInfo();
                BeanUtils.copyProperties(item, info);
                if(item.getDocConfig() != null){
                    info.setDocType(item.getDocConfig().getDocType());
                }
                return info;
            }).collect(Collectors.toList());
            return QueryResult.buildSuccessResult(list);
        }catch (BizException e){
            log.error("查询接口失败:appType={},appCode={}",appType,appCode,e);
            return QueryResult.error(e.getMsg());
        }

    }

    @Override
    public QueryResult<Pageable<JsfOrHttpMethodInfo>> queryJsfOrHttpMethodInfo(Long interfaceId, int current, int size) {

         return queryJsfOrHttpMethodInfo(interfaceId,current,size,false);

    }
    private static JsfOrHttpMethodInfo fromMethodManage(MethodManageDTO method){
        if(method == null) return null;
        JsfOrHttpMethodInfo info = new JsfOrHttpMethodInfo();
        BeanUtils.copyProperties(method, info);
        if(method.getContentObject()!=null){
            info.setContent(JsonUtils.toJSONString(method.getContentObject()));
        }
        info.setId(method.getId());
        if(method.getDocConfig() != null){
            info.setDocType(method.getDocConfig().getDocType());
            info.setInputExample(method.getDocConfig().getInputExample());
            info.setOutputExample(method.getDocConfig().getOutputExample());
        }
        info.setId(method.getId()+"");
        return info;
    }
    private static JsfOrHttpMethodInfo fromMethodManage(MethodManage method){
        JsfOrHttpMethodInfo info = new JsfOrHttpMethodInfo();
        BeanUtils.copyProperties(method, info);
        if(method.getContentObject()!=null){
            info.setContent(JsonUtils.toJSONString(method.getContentObject()));
        }
        if(method.getDocConfig() != null){
            info.setDocType(method.getDocConfig().getDocType());
            info.setInputExample(method.getDocConfig().getInputExample());
            info.setOutputExample(method.getDocConfig().getOutputExample());
        }
        info.setYn(method.getYn());
        info.setId(method.getId()+"");
        return info;
    }
    public QueryResult<Pageable<JsfOrHttpMethodInfo>> queryJsfOrHttpMethodInfo(Long interfaceId, int current, int size,boolean containsDeleted) {

        try{
            Page<MethodManage> pages = methodManageService.getInterfaceMethodsIncludeContent(interfaceId, Long.valueOf(current), Long.valueOf(size),containsDeleted);
            Pageable pageable = new Pageable();
            pageable.setCurrent(current);
            pageable.setSize(size);
            pageable.setTotal(Variant.valueOf(pages.getTotal()).toInt());
            List<JsfOrHttpMethodInfo> list = pages.getRecords().stream().map(record -> {
                JsfOrHttpMethodInfo info = fromMethodManage(record);
                return info;
            }).collect(Collectors.toList());
            pageable.setData(list);
            return QueryResult.buildSuccessResult(pageable);
        }catch (BizException e){
            log.error("查询接口失败:appType={}",interfaceId,e);
            return QueryResult.error(e.getMsg());
        }

    }

    @Override
    public QueryResult<Pageable<JsfOrHttpMethodInfo>> queryJsfOrHttpMethodInfoIncludeDeleted(Long interfaceId, int current, int size) {
        return queryJsfOrHttpMethodInfo(interfaceId,current,size,true);


    }

    @Override
    public QueryResult<JsfOrHttpMethodInfo> queryMethodById(Long id) {
        try {
            Guard.notEmpty(id, "id不能为空");
            MethodManageDTO method = methodManageService.getEntity(id+"");
            Guard.notEmpty(method,"无效的方法id");
            JsfOrHttpMethodInfo info = fromMethodManage(method);
            return QueryResult.buildSuccessResult(info);
        }catch (BizException e){
            log.error("查询接口失败:id={}",id,e);
            return QueryResult.error("查询方法失败"+e.getMsg());
        }catch (Exception e){
            log.error("查询接口失败:id={}",id,e);
            return QueryResult.error("查询方法失败"+e.getMessage());
        }
    }


     List<InterfaceTreeItem> tree2InterfaceItem(List<TreeSortModel> treeSortModels){
        List<InterfaceTreeItem> list = new ArrayList<>();
        for (TreeSortModel treeSortModel : treeSortModels) {
            InterfaceTreeItem item = new InterfaceTreeItem();
            item.setId(treeSortModel.getId());
            item.setName(treeSortModel.getName());
            item.setType(Variant.valueOf(treeSortModel.getType()).toInt(0));
            item.setEnName(treeSortModel.getEnName());
            item.setDeleted(false);
            if(treeSortModel instanceof GroupSortModel){

                item.setChildren(tree2InterfaceItem( ((GroupSortModel)treeSortModel).getChildren() ));
            }
            list.add(item);
        }
        return list;
    }

    @Override
    public QueryResult<List<InterfaceVersionDto>> listInterfaceVersion(Long interfaceId) {
        LambdaQueryWrapper<InterfaceVersion> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceVersion::getInterfaceId,interfaceId);
        lqw.select(InterfaceVersion::getId,InterfaceVersion::getVersion,InterfaceVersion::getCreated,InterfaceVersion::getCreator,InterfaceVersion::getDesc);
        lqw.orderByAsc(InterfaceVersion::getVersion);
        List<InterfaceVersion> versions = interfaceVersionService.list(lqw);
        List<InterfaceVersionDto> result = versions.stream().map(version -> {
            InterfaceVersionDto dto = new InterfaceVersionDto();
            dto.setId(version.getId());
            dto.setCreated(StringHelper.formatDate(version.getCreated(),"yyyy-MM-dd HH:mm:ss"));
            dto.setVersionDesc(version.getDesc());
            dto.setVersion(version.getVersion());
            return dto;
        }).collect(Collectors.toList());
        return QueryResult.buildSuccessResult(result);
    }

    @Override
    public QueryResult<JsfOrHttpMethodInfo> queryVersionMethodInfo(Long id, String version) {
        try{
            MethodManageDTO versionMethod = interfaceVersionService.getVersionMethod( version,id);
            return QueryResult.buildSuccessResult(fromMethodManage(versionMethod));
        }catch (Exception e){
            log.error("doc.err_export_doc:id={},version={}",id,version,e);
            return QueryResult.error("获取失败");
        }

    }

    @Override
    public QueryResult<List<InterfaceTreeItem>> queryLatestInterfaceTree(Long interfaceId) {

        try{
            return QueryResult.buildSuccessResult(queryInterfaceTree(interfaceId));
        }catch (Exception e){
            log.error("doc.err_getLastest_version_tree:interface_id={}",interfaceId,e);
            return QueryResult.error("获取失败");
        }

    }
    private List<InterfaceTreeItem> queryDeletedMethod(Long interfaceId){
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MethodManage::getYn,0);
        lqw.eq(MethodManage::getInterfaceId,interfaceId);
        methodManageService.excludeBigTextFiled(lqw);
        List<MethodManage> methods = methodManageService.list(lqw);

        return methods.stream().map(method->{
            InterfaceTreeItem treeItem = new InterfaceTreeItem();
            treeItem.setType(method.getType());
            treeItem.setDeleted(true);
            treeItem.setEnName(method.getMethodCode());
            treeItem.setName(method.getName());
            treeItem.setId(method.getId());
            treeItem.setInterfaceType(method.getType());
            return treeItem;
        }).collect(Collectors.toList());

    }
    private List<InterfaceTreeItem> queryInterfaceTree(Long interfaceId){
        MethodGroupTreeDTO dto = interfaceMethodGroupService.findMethodGroupTree(interfaceId);
        List<InterfaceTreeItem> treeItems = tree2InterfaceItem(dto.getTreeModel().getTreeItems());
        return treeItems;
    }

    @Override
    public QueryResult<List<InterfaceTreeItem>> queryLatestInterfaceAndMethodTree(String appCode) {
        AppInfo appInfo = appInfoService.findApp(appCode);

        if(appInfo == null){
            return QueryResult.buildSuccessResult(new ArrayList<>());
        }
        List<InterfaceManage> appInterfaces = interfaceManageService.getAppInterface(appInfo.getId());
        Collections.sort(appInterfaces, new Comparator<InterfaceManage>() {
            @Override
            public int compare(InterfaceManage o1, InterfaceManage o2) {
                return o1.getType() - o2.getType();
            }
        });
        List<InterfaceTreeItem> interfaces = appInterfaces.stream().map(item -> {
            InterfaceTreeItem tree = new InterfaceTreeItem();
            tree.setType(3);
            tree.setId(item.getId());
            tree.setName(item.getName());
            tree.setEnName(item.getServiceCode());
            tree.setInterfaceType(item.getType());
            tree.setChildren(new ArrayList<>());
            tree.setVersion(item.getLatestDocVersion());
            return tree;
        }).collect(Collectors.toList());
        List<Future> futures = new ArrayList<>();
        for (InterfaceTreeItem item : interfaces) {
            Future<?> future = defaultScheduledExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    List<InterfaceTreeItem> children = queryInterfaceTree(item.getId());
                    List<InterfaceTreeItem> interfaceItems = queryDeletedMethod(item.getId());
                    children.addAll(interfaceItems);
                    fixVersion(children,item.getVersion());
                    item.setChildren(children);

                }
            });
            futures.add(future);
        }
        for (Future future : futures) {
             try{
                 future.get();
             }catch (Exception e){
                 log.error("app.err_query_interface_tree:appCode={}",appCode,e);
                 return QueryResult.error("获取失败");
             }
        }
        return QueryResult.buildSuccessResult(interfaces);

    }
    private void fixVersion(List<InterfaceTreeItem> treeItems,String version ){
        if(treeItems == null) return;
        for (InterfaceTreeItem treeItem : treeItems) {
            treeItem.setVersion(version);
            fixVersion(treeItem.getChildren(),version);
        }
    }

    @Override
    public QueryResult<List<InterfaceTreeItem>> queryVersionInterfaceTree(Long interfaceId, String version) {
        try{
            if(StringUtils.isBlank(version)){
                return queryLatestInterfaceTree(interfaceId);
            }
            InterfaceManage manage = interfaceManageService.getById(interfaceId);
            Guard.notNull(manage,"无效的接口id");
            if(version.equals(manage.getLatestDocVersion())){
                return queryLatestInterfaceTree(interfaceId);
            }
            InterfaceVersion interfaceVersion = interfaceVersionService.getInterfaceVersion(interfaceId, version);
            List<InterfaceTreeItem> interfaceTreeItems = tree2InterfaceItem(interfaceVersion.getGroupTreeSnapshot().getTreeItems());
            return QueryResult.buildSuccessResult(interfaceTreeItems);
        }catch (Exception e){
            log.error("doc.err_getLastest_version_tree:interface_id={}",interfaceId,e);
            return QueryResult.error("获取失败");
        }
    }
    private List<TreeSortModel> interfaceTreeItemToSortModel(List<InterfaceTreeItem> treeItems){
        if(treeItems == null) return Collections.emptyList();
        return treeItems.stream().map(this::interfaceTreeItem2SortModel).collect(Collectors.toList());
    }
    private TreeSortModel interfaceTreeItem2SortModel(InterfaceTreeItem treeItem){
        if(treeItem == null){
            return null;
        }
        TreeSortModel sortModel = null;

        if(TreeSortModel.TYPE_INTERFACE.equals(treeItem.getType()+"")){
             sortModel = new InterfaceSortModel();

        }else if(TreeSortModel.TYPE_GROUP.equals(treeItem.getType() +"")){
            sortModel = new GroupSortModel();
        }else{
            sortModel = new MethodSortModel();
        }
        BeanUtils.copyProperties(treeItem,sortModel);
        sortModel.setType(treeItem.getType()+"");
        if(sortModel instanceof GroupSortModel){
            GroupSortModel groupSortModel = (GroupSortModel)sortModel;
            groupSortModel.setChildren(interfaceTreeItemToSortModel(treeItem.getChildren()));
        }
        return sortModel;
    }
    @Override
    public QueryResult<DocExportDto> exportDoc(String docType,String erp, List<InterfaceTreeItem> treeItems) {
        if(erp == null){
            throw new BizException("erp不能为空");
        }
        for (InterfaceTreeItem treeItem : treeItems) {
            if(!TreeSortModel.TYPE_INTERFACE.equals(treeItem.getType()+"")){
                return QueryResult.error("导出失败,最上级节点类型必须为3");
            }
        }
        List sortModels = interfaceTreeItemToSortModel(treeItems);
        String prefixName = "file_download_"+StringHelper.formatDate(new Date(),"yyyyMMdd");
        String prefix =  prefixName + "/" + UUID.randomUUID().toString();
        String bucketName = "lht";
        UserInfoInSession user = new UserInfoInSession();
        user.setUserId(erp);
        user.setUserName(erp);
        UserSessionLocal.setUser(user );
        try {
            if("md".equals(docType)){
                String result = docExportService.exportMd(sortModels);
                String url = jfsUtils.uploadContent(result, bucketName, prefix+".html");

                return QueryResult.buildSuccessResult(new DocExportDto("doc.md", url));
            }else if("pdf".equals(docType)) {
                DocExportDto dto = new DocExportDto();
                dto.setFileName("doc.pdf");
                 docExportService.exportPdf(sortModels,file->{
                     String url = jfsUtils.uploadFile(file.getPath(), bucketName, prefix + ".pdf");
                     dto.setDownloadUrl(url);
                     return null;
                 });
                return QueryResult.buildSuccessResult(dto);
            }else if("html".equals(docType)) {

                String result = docExportService.exportHtml(sortModels);
                String url = jfsUtils.uploadContent(result, bucketName, prefix+".html");
                return QueryResult.buildSuccessResult(new DocExportDto("doc.html", url));
            }else{
                return QueryResult.error("不支持的文档类型："+docType);
            }
        }catch (Exception e){
            log.error("doc.err_export_doc:treeItems={}", JsonUtils.toJSONString(treeItems),e);
            return QueryResult.error("导出失败");
        }

    }

    @Override
    public QueryResult<List<InterfaceVersionDto>> queryMethodVersions(Long methodId) {
        try{
            Guard.notEmpty(methodId,"无效的methodId");
            MethodManage method = methodManageService.getById(methodId);
            Guard.notEmpty(method,"无效的methodId");

            List<InterfaceVersion> extraVersions = new ArrayList<>();
            {
                LambdaQueryWrapper<InterfaceVersion> lqw = new LambdaQueryWrapper<>();
                lqw.eq(InterfaceVersion::getInterfaceId,method.getInterfaceId());
                lqw.le(InterfaceVersion::getCreated,method.getCreated());
                lqw.orderByAsc(InterfaceVersion::getId);
                lqw.select(InterfaceVersion::getVersion,InterfaceVersion::getId,InterfaceVersion::getCreated,InterfaceVersion::getDesc);
                lqw.last("limit 1");
                InterfaceVersion methodFirstCreatedVersion = interfaceVersionService.getOne(lqw);
                if(methodFirstCreatedVersion != null){
                    extraVersions.add(methodFirstCreatedVersion);
                }

            }
            { //消金想要最新的方法版本保留下来
                LambdaQueryWrapper<InterfaceVersion> lqw = new LambdaQueryWrapper<>();
                lqw.eq(InterfaceVersion::getInterfaceId,method.getInterfaceId());
                lqw.orderByDesc(InterfaceVersion::getId);
                lqw.select(InterfaceVersion::getVersion,InterfaceVersion::getId,InterfaceVersion::getCreated,InterfaceVersion::getDesc);
                lqw.last("limit 1");
                InterfaceVersion version = interfaceVersionService.getOne(lqw);
                if(version != null){
                    extraVersions.add(version);
                }

            }






            List<MethodVersionModifyLog> versions = methodVersionModifyLogService.listMethodVersions(methodId);
            List<InterfaceVersionDto> resultVersions = versions.stream().map(item -> {
                InterfaceVersionDto dto = new InterfaceVersionDto();
                dto.setVersion(item.getVersion());
                dto.setId(item.getId());
                dto.setCreated(StringHelper.formatDate(item.getCreated(), "yyyy-MM-dd HH:mm:ss"));
                return dto;
            }).collect(Collectors.toList());
            if(!extraVersions.isEmpty()){
                for (InterfaceVersion extraVersion : extraVersions) {
                    InterfaceVersionDto dto = new InterfaceVersionDto();
                    dto.setVersion(extraVersion.getVersion());
                    dto.setId(extraVersion.getId());
                    dto.setCreated(StringHelper.formatDate(method.getCreated(), "yyyy-MM-dd HH:mm:ss"));
                    boolean contains = resultVersions.stream().anyMatch(item -> item.getVersion().equals(dto.getVersion()));
                    if(!contains){
                        resultVersions.add(0,dto);
                    }
                }

            }
            List<String> versionNos = resultVersions.stream().map(item -> item.getVersion()).collect(Collectors.toList());

            if(!versionNos.isEmpty()){
                LambdaQueryWrapper<InterfaceVersion> versionLqw = new LambdaQueryWrapper<>();
                versionLqw.eq(InterfaceVersion::getInterfaceId,method.getInterfaceId());
                versionLqw.in(InterfaceVersion::getVersion,versionNos);
                versionLqw.orderByAsc(InterfaceVersion::getId);
                versionLqw.select(InterfaceVersion::getVersion,InterfaceVersion::getId,InterfaceVersion::getCreated,InterfaceVersion::getDesc);
                List<InterfaceVersion> versionList = interfaceVersionService.list(versionLqw);
                resultVersions = versionList.stream().map(item -> {
                    InterfaceVersionDto dto = new InterfaceVersionDto();
                    dto.setVersion(item.getVersion());
                    dto.setId(item.getId());
                    dto.setCreated(StringHelper.formatDate(item.getCreated(), "yyyy-MM-dd HH:mm:ss"));
                    dto.setVersionDesc(item.getDesc());
                    return dto;
                }).collect(Collectors.toList());
            }



            return QueryResult.buildSuccessResult(resultVersions);
        }catch (BizException e){
            log.error("doc.err_query_method_versions:method_id={}",methodId,e);
            return QueryResult.error(e.getMsg());
        }

    }


    @Override
    public QueryResult<Boolean> cancelDelete(Long methodId) {
        try{
            Guard.notEmpty(methodId,"methodId无效");
            MethodManage method = methodManageService.getById(methodId);
            Guard.notNull(method,"methodId无效");
            if(method.getYn() != 0){
                return QueryResult.error("该方法状态为非删除状态，无法取消删除");
            }

            LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
            lqw.eq(MethodManage::getInterfaceId,method.getInterfaceId());
            lqw.eq(MethodManage::getPath,method.getPath());
            lqw.eq(MethodManage::getYn,1);
            lqw.eq(MethodManage::getHttpMethod,method.getHttpMethod());
            lqw.select(MethodManage::getId);
            List<MethodManage> exist = methodManageService.list(lqw);
            if(exist != null && exist.size() > 0){
                return QueryResult.error("同路径的方法已经存在，无法取消删除");
            }


            LambdaUpdateWrapper<MethodManage> luw = new LambdaUpdateWrapper<>();
            luw.eq(MethodManage::getId,methodId);
            luw.set(MethodManage::getYn,1);
            methodManageService.update(luw);
            return QueryResult.buildSuccessResult(true);
        }catch (BizException e){
            log.error("取消删除接口失败:methodId={}",methodId,e);
            return QueryResult.error(e.getMsg());
        }

    }
    private List<AppInfo> getAppIdsByJdosAppCodes(List<String> jdosAppCodes){
        LambdaQueryWrapper<AppInfo> lqw = new LambdaQueryWrapper<>();
        lqw.in(AppInfo::getJdosAppCode,jdosAppCodes);
        lqw.select(AppInfo::getId,AppInfo::getAppCode,AppInfo::getJdosAppCode);
        List<AppInfo> appInfos = appInfoService.list(lqw);
        return appInfos;
    }

    @Override
    public QueryResult<List<AppHasInterfaceResult>> queryAppHasInterfaceResult(List<String> jdosAppCodes) {
        if(jdosAppCodes.isEmpty()){
            return QueryResult.buildSuccessResult(Collections.emptyList());
        }
        List<AppInfo> appInfos = getAppIdsByJdosAppCodes(jdosAppCodes);
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.in(InterfaceManage::getAppId,appInfos.stream().map(item->item.getId()).collect(Collectors.toList()));
        lqw.select(InterfaceManage::getId,InterfaceManage::getAppId);
        List<InterfaceManage> interfaceManages = interfaceManageService.list(lqw);
        List<AppHasInterfaceResult> list = new ArrayList<>();
        for (String jdosAppCode : jdosAppCodes) {
            boolean hasInterface = false;
            for (InterfaceManage interfaceManage : interfaceManages) {
                for (AppInfo appInfo : appInfos) {
                    if(interfaceManage.getAppId().equals(appInfo.getId() )){
                        hasInterface = true;
                    }
                }
            }
            list.add(new AppHasInterfaceResult(jdosAppCode,hasInterface));
        }
        return QueryResult.buildSuccessResult(list);
    }

    @Override
    public QueryResult<Boolean> removeJsfAuth(String appCode, String interfaceName) {
        log.info("interface.remove_jsf_auth:appCode={},interfaceName={}",appCode,interfaceName);
        AppInfo app = appInfoService.findApp(appCode);
        if(app == null){
            return QueryResult.buildSuccessResult(false);
        }
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceManage::getServiceCode,interfaceName);
        lqw.eq(InterfaceManage::getYn,1);
        lqw.eq(InterfaceManage::getAppId,app.getId());
        interfaceManageService.excludeBigTextFiled(lqw);
        List<InterfaceManage> list = interfaceManageService.list(lqw);
        for (InterfaceManage interfaceManage : list) {
            if(StringUtils.isNotBlank(interfaceManage.getCjgAppId())){
                LambdaUpdateWrapper<InterfaceManage> luw = new LambdaUpdateWrapper<>();
                luw.set(InterfaceManage::getCjgAppId,null);
                luw.eq(InterfaceManage::getId,interfaceManage.getId());
                log.info("interface.remove_jsf_auth:appCode={},interfaceId={}",appCode,interfaceManage.getId());
                interfaceManageService.update(luw);
            }
        }

        return QueryResult.buildSuccessResult(true);
    }

    private JsfDocInfo buildJsfDocInfo(Long id){
        JsfDocInfo docInfo = new JsfDocInfo();
        docInfo.setId(id);
        String url = "http://console.paas.jd.com/idt/online/interfaceList/interfaceDetail/3/{interfaceId}/1";
        docInfo.setUrl(url.replace("{interfaceId}",id+""));
        return docInfo;
    }


}
