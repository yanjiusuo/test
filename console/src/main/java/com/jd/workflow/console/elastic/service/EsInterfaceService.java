package com.jd.workflow.console.elastic.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.ResourceTypeEnum;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.dto.manage.InterfaceMarketSearchResult;
import com.jd.workflow.console.elastic.entity.ElasticConstants;
import com.jd.workflow.console.elastic.entity.InterfaceManageDoc;
import com.jd.workflow.console.elastic.entity.MethodManageDoc;
import com.jd.workflow.console.elastic.repository.InterfaceManageRepository;
import com.jd.workflow.console.elastic.repository.MethodManageRepository;
import com.jd.workflow.console.elastic.repository.impl.MethodManageRepositoryImpl;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MemberRelation;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.impl.InterfaceManageServiceImpl;
import com.jd.workflow.console.service.impl.MemberRelationServiceImpl;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.xml.schema.ComplexJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EsInterfaceService {
    @Autowired
    InterfaceManageRepository interfaceManageRepository;
    @Autowired
    MethodManageServiceImpl methodManageService;
    @Autowired
    MethodManageRepositoryImpl methodManageRepository;
    @Autowired
    InterfaceManageServiceImpl interfaceManageService;
    @Autowired
    MemberRelationServiceImpl memberRelationService;
    @Autowired
    IAppInfoService appInfoService;
    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Resource(name = "docThreadExecutor")
    ScheduledThreadPoolExecutor scheduleService;

    //线程太多的话会很慢
    ThreadPoolExecutor indexRebuildExecutor;

    @PostConstruct
    public void init(){
        indexRebuildExecutor = new ThreadPoolExecutor(2,2,
                60, TimeUnit.SECONDS,new LinkedBlockingQueue()
                );
    }

    public  InterfaceManageDoc indexInterfaceManageDoc(InterfaceManageDoc interfaceManageDoc){
        InterfaceManageDoc result = (InterfaceManageDoc) interfaceManageRepository.save(interfaceManageDoc);
        return result;
    }
    private <T> List<T> toList(Iterator<T> t){
        List<T> result = new ArrayList<>();
        while (t.hasNext()){
            result.add(t.next());
        }
        return result;
    }
    public List<InterfaceManageDoc> listAllInterfaces(){
        Iterator<InterfaceManageDoc> iterator = interfaceManageRepository.findAll().iterator();
        return toList(iterator);
    }
    public List<MethodManageDoc> listAllMethods(){
        Iterator<MethodManageDoc> iterator = methodManageRepository.findAll().iterator();
        return toList(iterator);
    }
    public void initAllAppIndex(){
        LambdaQueryWrapper<AppInfo> lqw = new LambdaQueryWrapper<>();
        lqw.inSql(AppInfo::getId,"select app_id from interface_manage where yn = 1 and type=1 or type = 3");
        lqw.eq(AppInfo::getYn,1);
        List<AppInfo> appInfos = appInfoService.list(lqw);
        log.info("index.begin_rebuild_index:appSize={}",appInfos.size());
        for (AppInfo appInfo : appInfos) {
            indexRebuildExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try{
                       rebuildAppIndex(appInfo.getId());
                    }catch (Exception e){
                        log.error("app.rebuild_app_index_fail:appCode={}",appInfo.getAppCode(),e);
                    }
                }
            });
        }
        final long start = System.currentTimeMillis();
        indexRebuildExecutor.execute(new Runnable() {
            @Override
            public void run() {
                log.info("rebuild_index_cost_time:{}",System.currentTimeMillis() - start);
            }
        });
    }
    public  void rebuildAppIndex(Long appId){
        try{
            log.info("es.rebuild_app_index:appId={}",appId);
            long start = System.currentTimeMillis();
            removeAppDoc(appId);
            saveAppDoc(appId);
            log.info("es.rebuild_app_index_end:appId={},costTime={}",appId,System.currentTimeMillis() - start);
        }catch (Exception e){
            log.error("app.err_rebuild_index:appId={}",appId,e);
        }
    }
    public void saveAppDoc(Long appId){
        AppInfo appInfo = appInfoService.getById(appId);
        List<InterfaceManage> interfaceManages = interfaceManageService.getAppInterfaces(appId);

        saveInterfaceDoc(interfaceManages,appInfo);
        for (InterfaceManage interfaceManage : interfaceManages) {
            rebuildInterfaceMethodIndex(interfaceManage);
        }
    }
    public void removeAppDoc(Long appId){
        List<InterfaceManage> appInterfaces = interfaceManageService.getAppInterfaceIncludeInvalids(appId);
        List<Long> ids = appInterfaces.stream().map(item -> item.getId()).collect(Collectors.toList());
        removeInterfaceDoc(ids);
        //removeInterfaceMethodDoc(ids);
    }

    public void removeInterfaceDoc(List<Long> ids){
        String indexName = ElasticConstants.INTERFACE_INDEX_ALIAS;
        DeleteByQueryRequest request = new DeleteByQueryRequest(indexName);
        request.setQuery(QueryBuilders.termsQuery("interfaceId",ids));
        //request.setRefresh(true); // 可选，设置为true以使更改立即可见

        /*DeleteByQueryRequestBuilder deleteByQueryRequestBuilder = new DeleteByQueryRequestBuilder(restHighLevelClient, DeleteByQueryAction.INSTANCE)
                .source(indexName)
                .abortOnVersionConflict(false)
                .filter(QueryBuilders.termQuery(field, value));*/

        try {
            BulkByScrollResponse response = restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
            response.getDeleted();
            log.info("es.remove_interface_index_result:response={}",response);
            removeInterfaceMethodDoc(ids);
        } catch (IOException e) {
            throw new BizException("es.err_remove_index_doc",e);
        }
    }
    public void removeMethodDoc(List<Long> ids){
        String indexName = ElasticConstants.METHOD_INDEX_ALIAS;
        DeleteByQueryRequest request = new DeleteByQueryRequest(indexName);
        request.setQuery(QueryBuilders.termsQuery("methodId", ids));


        try {
            BulkByScrollResponse response = restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
            response.getDeleted();
            log.info("es.remove_method_index_result:response={}",response);
        } catch (IOException e) {
            throw new BizException("es.err_remove_index_doc",e);
        }
    }
    public void removeInterfaceMethodDoc(List<Long> ids){
        String indexName = ElasticConstants.METHOD_INDEX_ALIAS;
        DeleteByQueryRequest request = new DeleteByQueryRequest(indexName);
        request.setQuery(QueryBuilders.termsQuery("interfaceId", ids));


        try {
            BulkByScrollResponse response = restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
            response.getDeleted();
            log.info("es.remove_method_index_result:",response);
        } catch (IOException e) {
            throw new BizException("es.err_remove_index_doc",e);
        }
    }
    private Set<String> getMembers(InterfaceManage interfaceManage, AppInfo appInfo){
        Set<String> result = new HashSet<>();
        List<MemberRelation> memberRelations = memberRelationService.listByInterfaceId(interfaceManage.getId());
        AppInfoDTO dto = new AppInfoDTO();
        result.addAll(dto.splitMembers(appInfo.getMembers()));
        for (MemberRelation memberRelation : memberRelations) {
            result.add(memberRelation.getUserCode());
        }
        return result;
    }
    public List<InterfaceManageDoc> saveInterfaceDoc(List<InterfaceManage> interfaceManages, AppInfo appInfo){
        List<InterfaceManageDoc> docs = new ArrayList<>();
        for (InterfaceManage interfaceManage : interfaceManages) {

            InterfaceManageDoc doc = InterfaceManageDoc.from(interfaceManage,appInfo,getMembers(interfaceManage,appInfo));
            docs.add(doc);
        }

        return (List<InterfaceManageDoc>) interfaceManageRepository.saveAll(docs);
    }

    public List<MethodManageDoc> saveMethodDoc(List<MethodManage> methods,InterfaceManage interfaceManage){
        List<MethodManageDoc> docs = new ArrayList<>();
        for (MethodManage method : methods) {
            MethodManageDoc doc = newMethodDoc(method,interfaceManage);
            docs.add(doc);
        }

        return (List<MethodManageDoc>) methodManageRepository.saveAll(docs);
    }
    public void rebuildInterfaceMethodIndex(InterfaceManage interfaceManage){

        int start = 1;
        removeInterfaceMethodDoc(Collections.singletonList(interfaceManage.getId()));
        while (true){
            boolean end = rebuildInterfaceMethodIndex(start,interfaceManage);
            if(end) break;
            start++;
        }
    }

    private boolean rebuildInterfaceMethodIndex(int start,InterfaceManage interfaceManage){
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MethodManage::getYn,1);
        lqw.eq(MethodManage::getInterfaceId,interfaceManage.getId());
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<MethodManage> page = methodManageService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(start, 50),lqw);
        if(page.getRecords().isEmpty()) return true;
        methodManageService.initMethodRefAndDelta(page.getRecords(),interfaceManage.getAppId());
        methodManageRepository.saveAll(page.getRecords().stream().map(item->newMethodDoc(item,interfaceManage)).collect(Collectors.toList()));
        return false;
    }
    public List<MethodManageDoc> saveOrUpdateMethodDoc(List<MethodManage> methods,InterfaceManage interfaceManage){
        removeMethodDoc(methods.stream().map(item->item.getId()).collect(Collectors.toList()));

        List<MethodManageDoc> docs = new ArrayList<>();
        for (MethodManage method : methods) {
            MethodManageDoc doc = newMethodDoc(method,interfaceManage);
            docs.add(doc);
        }

        return (List<MethodManageDoc>) methodManageRepository.saveAll(docs);
    }
    public void saveOrUpdateInterface(List<InterfaceManage> interfaceManages, AppInfo appInfo){
        List<Long> interfaceIds = interfaceManages.stream().map(item->item.getId()).collect(Collectors.toList());
        List<InterfaceManageDoc> interfaceManageDocs = interfaceManageRepository.listByIds(interfaceIds);
        interfaceManageRepository.deleteAll(interfaceManageDocs);

        List<InterfaceManageDoc> docs = new ArrayList<>();
        for (InterfaceManage interfaceManage : interfaceManages) {
            InterfaceManageDoc doc = InterfaceManageDoc.from(interfaceManage,appInfo,getMembers(interfaceManage,appInfo));
            docs.add(doc);
        }
        interfaceManageRepository.saveAll(docs);
    }
    public MethodManageDoc newMethodDoc(MethodManage methodManage,InterfaceManage manage){
        MethodManageDoc doc = new MethodManageDoc();
        doc.setMethodId(methodManage.getId());
        doc.setInterfaceId(methodManage.getInterfaceId());
        doc.setVisibility(manage.getVisibility());
        BeanUtils.copyProperties(methodManage,doc);
        methodManageService.initContentObject(methodManage);
        doc.setPath(methodManage.getPath());
        doc.setContent(buildMethodContent(methodManage));
        doc.setServiceCode(manage.getServiceCode());
        doc.setServiceName(manage.getName());
        doc.setAppId(manage.getAppId());
        doc.setId(methodManage.getId().toString());
        doc.setDeptName(manage.getDeptName());
        AppInfo appInfo = appInfoService.getById(manage.getAppId());
        if(Objects.nonNull(appInfo)){
            doc.setAppCode(appInfo.getAppCode());
            doc.setAppName(appInfo.getAppName());
        }
        return doc;
    }
    private String buildMethodContent(MethodManage methodManage){
        StringBuilder sb = new StringBuilder();
        if(InterfaceTypeEnum.HTTP.getCode().equals(methodManage.getType())){
            HttpMethodModel httpMethodModel = (HttpMethodModel) methodManage.getContentObject();
            buildContent(httpMethodModel.getInput().getParams(), sb);
            buildContent(httpMethodModel.getInput().getPath(), sb);
            buildContent(httpMethodModel.getInput().getBody(), sb);
            buildContent(httpMethodModel.getInput().getHeaders(), sb);
            buildContent(httpMethodModel.getOutput().getHeaders(), sb);
            buildContent(httpMethodModel.getOutput().getBody(), sb);

        }else if(InterfaceTypeEnum.JSF.getCode().equals(methodManage.getType())){
            JsfStepMetadata jsfStepMetadata = (JsfStepMetadata) methodManage.getContentObject();
            buildContent(jsfStepMetadata.getInput(),sb);
            buildContent(jsfStepMetadata.getOutput(),sb);
        }
        return sb.toString();
    }
    private void buildContent(JsonType jsonType,StringBuilder sb){
        if(jsonType == null) return;
        sb.append(jsonType.getName());
        if(StringUtils.isNotBlank(jsonType.getDesc())){
            sb.append(":");
            sb.append(jsonType.getDesc());
        }
        sb.append(",");
        if(jsonType instanceof ComplexJsonType){
            buildContent(((ComplexJsonType) jsonType).getChildren(),sb);
        }
        buildContent(jsonType.getGenericTypes(),sb);
    }

    private void buildContent(List<? extends JsonType> jsonTypes,StringBuilder sb){
        if(jsonTypes == null) return;
        for (JsonType jsonType : jsonTypes) {
            buildContent(jsonType, sb);
        }
    }

    public  InterfaceManageDoc indexMethodDoc(InterfaceManageDoc interfaceManageDoc){
        InterfaceManageDoc result = (InterfaceManageDoc) interfaceManageRepository.save(interfaceManageDoc);
        return result;
    }
    public Page<InterfaceManageDoc> searchInterface(String search,Integer type, int current, int pageSize){
        return interfaceManageRepository.searchInterface(search,type,current,pageSize);
    }

    public InterfaceMarketSearchResult searchOnlyInterface(String search, int current, int pageSize){
        search = search.toLowerCase();
        Page<InterfaceManageDoc> interfaceManages = searchInterface(search,InterfaceTypeEnum.JSF.getCode(), current, pageSize);
        List<Long> ids = interfaceManages.stream().map(item -> item.getInterfaceId()).collect(Collectors.toList());
        InterfaceMarketSearchResult result = new InterfaceMarketSearchResult();
        result.setType(InterfaceTypeEnum.JSF_INTERFACE.getCode());
        result.setCount(ids.size());
        List<InterfaceManage> interfaces = interfaceManageService.listInterfaceByIds(ids);
        interfaceManageService.fixInterfaceAdminInfo(interfaces, ResourceTypeEnum.INTERFACE.getCode());
        interfaceManageService.fillInterfaceAppInfo(interfaces);
        interfaceManageService.fixHasLicense(interfaces);
        List<InterfaceMarketSearchResult.MethodResult> list = new ArrayList<>();
        for (InterfaceManage interfaceManage : interfaces) {
            InterfaceMarketSearchResult.MethodResult item = new InterfaceMarketSearchResult.MethodResult();
            item.setInterfaceName(interfaceManage.getName());
            item.setInterfaceServiceCode(interfaceManage.getServiceCode());
            item.setAppId(interfaceManage.getAppId());
            item.setInterfaceId(interfaceManage.getId());
            item.setAppName(interfaceManage.getAppName());
            item.setAppCode(interfaceManage.getAppCode());
            item.setUserCode(interfaceManage.getUserCode());
            item.setUserName(interfaceManage.getUserName());
            item.setDept(interfaceManage.getDeptName());
            item.setScore(interfaceManage.getScore());
            item.setHasLicense(interfaceManage.getHasLicense());
            item.setCloudFileTags(interfaceManage.getCloudFileTags());
            list.add(item);
        }
        result.setData(list);
        return result;
    }

    public InterfaceMarketSearchResult searchMethodOrInterface(String search,Integer type, int page, int size){
        Page<InterfaceManageDoc> interfaceManages = searchInterface(search,type, 1, 100);
        List<Long> ids = interfaceManages.stream().map(item -> item.getInterfaceId()).collect(Collectors.toList());
        Page<MethodManageDoc> pageMethods = methodManageRepository.searchMethod(ids,type, search, page, size);
        InterfaceMarketSearchResult result = new InterfaceMarketSearchResult();
        result.setType(type);
        result.setCount(pageMethods.getTotalElements());
        List<Long> methodIds = pageMethods.stream().map(item->item.getMethodId()).collect(Collectors.toList());

        List<MethodManage> methods = methodManageService.listMethods(methodIds);

        List<InterfaceManage> interfaces = interfaceManageService.listInterfaceByIds(methods.stream().map(item->item.getInterfaceId()).collect(Collectors.toList()));
        interfaceManageService.fixInterfaceAdminInfo(interfaces, ResourceTypeEnum.INTERFACE.getCode());
        interfaceManageService.fillInterfaceAppInfo(interfaces);
        interfaceManageService.fixHasLicense(interfaces);
        Map<Long, List<InterfaceManage>> id2Interfaces = interfaces.stream().collect(Collectors.groupingBy(InterfaceManage::getId));
        Map<Long, List<MethodManage>> id2Methods = methods.stream().collect(Collectors.groupingBy(MethodManage::getId));

        List<InterfaceMarketSearchResult.MethodResult> list = new ArrayList<>();

        for (MethodManageDoc pageMethod : pageMethods) {
            MethodManage method = null;
            if(id2Methods.containsKey(pageMethod.getMethodId())){
                method = id2Methods.get(pageMethod.getMethodId()).get(0);
            }

            InterfaceMarketSearchResult.MethodResult item = new InterfaceMarketSearchResult.MethodResult();
            item.setMethodId(pageMethod.getMethodId());
            if(method!=null){
                InterfaceManage interfaceManage = null;
                if(id2Interfaces.containsKey(method.getInterfaceId())){
                   interfaceManage =  id2Interfaces.get(method.getInterfaceId()).get(0);
                }

                item.setMethodName(method.getName());
                item.setMethodCode(method.getMethodCode());
                item.setHttpPath(method.getPath());
                item.setDocInfo( pageMethod.getDocInfo());
                item.setInterfaceId(method.getInterfaceId());
                item.setCloudFileTags(method.getCloudFileTags());
                if(interfaceManage != null){
                    item.setInterfaceName(interfaceManage.getName());
                    item.setInterfaceServiceCode(interfaceManage.getServiceCode());
                    item.setAppId(interfaceManage.getAppId());
                    item.setAppName(interfaceManage.getAppName());
                    item.setAppCode(interfaceManage.getAppCode());
                    item.setUserCode(interfaceManage.getUserCode());
                    item.setUserName(interfaceManage.getUserName());
                    item.setDept(interfaceManage.getDeptName());
                    item.setScore(interfaceManage.getScore());

                    item.setHasLicense(interfaceManage.getHasLicense());
                }
            }
            item.setParamDesc(pageMethod.getContent());
            list.add(item);
        }
        result.setData(list);
        return result;
    }
}
