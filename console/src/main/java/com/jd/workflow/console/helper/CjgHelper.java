package com.jd.workflow.console.helper;

import com.google.common.collect.Lists;
import com.jd.cjg.bus.BusInterfaceRpcService;
import com.jd.cjg.bus.request.ComponentCreateReq;
import com.jd.cjg.bus.request.ComponentUpdateReq;
import com.jd.cjg.bus.request.InterfaceCreateReq;
import com.jd.cjg.bus.request.MethodAddReq;
import com.jd.cjg.bus.request.MethodDeleteReq;
import com.jd.cjg.bus.request.MethodSearchReq;
import com.jd.cjg.bus.request.SearchComponentReq;
import com.jd.cjg.bus.vo.ComponentInfoVo;
import com.jd.cjg.bus.vo.InterfaceDetailsVo;
import com.jd.cjg.bus.vo.MethodInfoVo;
import com.jd.cjg.kg.KgBusinessDomainProvider;
import com.jd.cjg.kg.vo.KgBusinessDomainVo;
import com.jd.cjg.result.CjgResult;
import com.jd.cjg.result.PageResult;
import com.jd.cjg.result.Result;
import com.jd.cjg.result.StatusMessage;
import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.QueryAppReqDTO;
import com.jd.workflow.console.dto.QueryAppResultDTO;
import com.jd.workflow.console.dto.app.CjgAppType;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目名称：parent
 * 类 名 称：CjgHelper
 * 类 描 述：cjg应用信息同步
 * 创建时间：2022-11-28 17:17
 * 创 建 人：wangxiaofei8
 */
@Service
@Slf4j
public class CjgHelper {

    @Autowired(required = false)
    private BusInterfaceRpcService busAppInterfaceRpcService;
    @Autowired(required = false)
    private KgBusinessDomainProvider businessDomainProvider;

    private List<String> nonNullList(List<String> list){
        if(list == null) return new ArrayList<>();
        return list;
    }

    /**
     * 创建应用
     * @param app
     * @return
     */
    public String createCjgComponent(AppInfoDTO app){
        ComponentCreateReq req = new ComponentCreateReq();
        req.setAppCode(app.getAppCode());
        req.setAppName(app.getAppName());
        req.setAuthLevel(app.getAuthLevel());
        req.setProjectManager(app.getOwner().get(0));
        req.setDeveloperList(nonNullList(app.getMember()));
        req.setTesterRelList(nonNullList(app.getTestMember()));
        req.setProductManagerList(nonNullList(app.getProductor()));
        if(ObjectHelper.isEmpty(app.getTester())){
            req.setTesterList(Lists.newArrayList(app.getOwner().get(0)));
        }else{
            req.setTesterList(nonNullList(app.getTester()));
        }


        req.setCreateBy(UserSessionLocal.getUser().getUserId());
        req.setDevLanguage(app.getDevLanguage());
        if(app.getAppTypeReqList() != null){
            req.setAppTypeReqList(app.getAppTypeReqList().stream().map(item->item.toReq()).collect(Collectors.toList()));
        }

        req.setDesc(app.getDesc());

        Result<Boolean> result = null;
        try {
            log.info("busInterfaceRpcService.createComponent request={} ", JSON.toJSONString(req));
            result = busAppInterfaceRpcService.createComponent(req);
            log.info("busInterfaceRpcService.createComponent response={} ", JSON.toJSONString(result));
        } catch (Exception e) {
            log.error("busInterfaceRpcService.createComponent call occur exception>>>>>> ", e);
            throw new BizException("远程服务调用异常！",e);
        }
        if(result==null||result.getCode()!=StatusMessage.SUCCESS.getCode()|| !BooleanUtils.isTrue(result.getModel())){
           throw new BizException("创建应用cjg远程服务调用返回失败:"+result.getMessage());
        }
        return app.getAppCode();
    }

    /**
     * 根据藏经阁trace字段获取domain信息
     * @param traceCode
     * @return
     */
    public KgBusinessDomainVo getDomainByTrace(String traceCode){
        try{
            CjgResult<KgBusinessDomainVo> result = businessDomainProvider.getDomainByTrace(traceCode);
            if(result.checkSuccess()){
                return result.getModel();
            }
            log.error("cjg.err_get_domain:trace={},result={}",traceCode, JsonUtils.toJSONString(result));
            return null;
        }catch (Exception e){
            log.error("cjg.err_get_domain:traceCode={}",traceCode,e);
            return null;
        }

    }
    /**
     * 模糊查询应用列表
     * @param query
     * @return
     */
    public QueryAppResultDTO queryComponent(QueryAppReqDTO query){
        QueryAppResultDTO result = new QueryAppResultDTO();
        SearchComponentReq req = new SearchComponentReq();
        req.setName(query.getAppName());
        req.setErp(query.getPin());
        req.setStart(((query.getCurrentPage()-1)*query.getPageSize()));
        req.setPageSize(query.getPageSize());
        Result<PageResult<ComponentInfoVo>> response = null;
        try {
            log.info("busInterfaceRpcService.getComponentInfoByParam request={} ", JSON.toJSONString(req));
            response = busAppInterfaceRpcService.getComponentInfoByParam(req);
            log.info("busInterfaceRpcService.getComponentInfoByParam response={} ", JSON.toJSONString(response));
            if(response!=null&&response.getModel()!=null){
                result.setTotalCnt(response.getModel().getRecordsTotal());
                if(response.getModel().getData()!=null){
                    result.setList(response.getModel().getData().stream().map(obj->{
                        AppInfoDTO dto = new AppInfoDTO();
                        dto.setCjgAppId(obj.getAppCode());
                        dto.setAppCode(obj.getAppCode());
                        dto.setAppName(obj.getAppName());
                        dto.setAuthLevel(obj.getAuthLevel());
                        dto.setOwner(Lists.newArrayList(obj.getProjectManager()));
                        dto.setProductor(obj.getProductManagerList());
                        dto.setTester(obj.getTesterList());
                        return dto;
                    }).collect(Collectors.toList()));
                }
            }
        } catch (Exception e) {
            log.error("busInterfaceRpcService.getComponentInfoByParam call occur exception>>>>>> ", e);
        }
        result.setPageSize(query.getPageSize());
        result.setCurrentPage(query.getCurrentPage());
        return result;


    }

    /**
     * 更新应用
     * @param dto
     * @return
     */
    public boolean updateComponentByCode(AppInfoDTO dto){


        Result<ComponentInfoVo> result = busAppInterfaceRpcService.getComponentInfoByCode(dto.getAppCode());
        log.info("app.get_cjg_app_info:cjgAppCode={},result={}", dto.getAppCode(), JsonUtils.toJSONString(result));
        ComponentInfoVo vo = result.getModel();
        if (result == null || vo == null) return false ;





        ComponentUpdateReq req = new ComponentUpdateReq();
        {
            Set<String> tester = new HashSet<>();
            tester.addAll(nonNullList(dto.getTester()));
            tester.addAll(vo.getTesterList());
            req.setTesterList(tester.stream().collect(Collectors.toList()));
        }
        { // 保留藏经阁原有的测试成员
            Set<String> testerRelList = new HashSet<>();
            testerRelList.addAll(nonNullList(dto.getTestMember()));
            testerRelList.addAll(vo.getTesterRelList());
            req.setTesterRelList(testerRelList.stream().collect(Collectors.toList()));
        }
        { // 保留藏经阁原有的成员信息
            Set<String> members = new HashSet<>();
            members.addAll(nonNullList(dto.getMember()));
            members.addAll(vo.getDeveloperList());
            members.add(dto.getJdosOwner());
            req.setDeveloperList(members.stream().collect(Collectors.toList()));
        }
        {
            Set<String> productor = new HashSet<>();
            productor.addAll(nonNullList(dto.getProductor()));
            if(vo.getProductManagerList() != null){
                productor.addAll(vo.getProductManagerList());
            }

            req.setProductManagerList(productor.stream().collect(Collectors.toList()));
        }
        req.setAppCode(dto.getAppCode());
        req.setAppName(dto.getAppName());
        req.setModifyBy(dto.getModifier());
        req.setProjectManager(dto.getOwner().get(0));

        //req.setProductManagerList(nonNullList(dto.getProductor())); // 产品负责人

        req.setDesc(dto.getDesc());
        req.setDevLanguage(dto.getDevLanguage());
        if(dto.getAppTypeReqList()!=null){
            req.setAppTypeReqList(dto.getAppTypeReqList().stream().map(item->item.toReq()).collect(Collectors.toList()));
        }

        req.setModifyBy(UserSessionLocal.getUser()==null?null:UserSessionLocal.getUser().getUserId());
        Result<Boolean> response = null;
        try {
            log.info("busInterfaceRpcService.updateComponentByCode request={} ", JSON.toJSONString(req));
            response = busAppInterfaceRpcService.updateComponentByCode(req);
            log.info("busInterfaceRpcService.updateComponentByCode response={} ", JSON.toJSONString(response));
            if(response==null||response.getCode()!=StatusMessage.SUCCESS.getCode()){
                log.error("busInterfaceRpcService.updateComponentByCode call return data exception>>>>>> request={},response={} ",req, JSON.toJSONString(response));
                return false;
            }
            return BooleanUtils.isTrue(response.getModel());
        } catch (Exception e) {
            log.error("busInterfaceRpcService.updateComponentByCode call occur exception>>>>>> ", e);
            return false;
        }
    }

    /**
     * 应用查询
     * @param appCode
     * @return
     */
    public AppInfoDTO getCjgComponetInfoByCode(String appCode){
        Result<ComponentInfoVo> componentInfo = null;
        try {
            log.info("busInterfaceRpcService.getComponentInfoByCode request={} ", appCode);
            componentInfo = busAppInterfaceRpcService.getComponentInfoByCode(appCode);
            log.info("busInterfaceRpcService.getComponentInfoByCode response={} ", JSON.toJSONString(componentInfo));
        } catch (Exception e) {
            log.error("busInterfaceRpcService.getComponentInfoByCode call occur exception>>>>>> ", e);
            throw new BizException("远程服务调用异常！",e);
        }
        if(componentInfo==null||componentInfo.getCode()!=StatusMessage.SUCCESS.getCode()){
            throw new BizException("调用cjg远程服务查询应用信息返回失败！");
        }
        if(componentInfo.getModel()==null){
            return null;
        }
        AppInfoDTO dto = new AppInfoDTO();
        dto.setAppCode(componentInfo.getModel().getAppCode());
        dto.setCjgAppKey(componentInfo.getModel().getId()+"");
        dto.setAppName(componentInfo.getModel().getAppName());
        dto.setAuthLevel(componentInfo.getModel().getAuthLevel());
        dto.setOwner(Lists.newArrayList(componentInfo.getModel().getProjectManager()));
        dto.setMember(componentInfo.getModel().getDeveloperList());
        dto.setTestMember(componentInfo.getModel().getTesterRelList());
        dto.setProductor(componentInfo.getModel().getProductManagerList());
        dto.setTester(componentInfo.getModel().getTesterList());
        dto.setDevLanguage(componentInfo.getModel().getDevLanguage());
        dto.setAuthLevel(componentInfo.getModel().getAuthLevel());
        if(componentInfo.getModel().getAppTypeVoList() != null){
            dto.setAppTypeReqList(componentInfo.getModel().getAppTypeVoList().stream().map(item-> CjgAppType.from(item)).collect(Collectors.toList()));
        }


        return dto;
    }


    public Boolean addMethod(InterfaceManage interfaceManage, MethodManage methodManage,String operator){
        //String operator = UserSessionLocal.getUser().getUserId();
        MethodAddReq req = new MethodAddReq();
        req.setMethodName(methodManage.getMethodCode());
        req.setMethodCName(methodManage.getName());
        req.setAppCode(interfaceManage.getCjgAppId());
        req.setInterfaceName(interfaceManage.getServiceCode());
        req.setCreateBy(operator);
        Result<Boolean> result = null;
        try {
            log.info("busInterfaceRpcService.addMethod request={} ", req);
            result = busAppInterfaceRpcService.addMethod(req);
            log.info("busInterfaceRpcService.addMethod response={} ", JSON.toJSONString(result));
        } catch (Exception e) {
            log.error("busInterfaceRpcService.addMethod call occur exception>>>>>> ", e);
            throw new BizException("远程服务调用异常！");
        }
        if(result==null||result.getCode()!=StatusMessage.SUCCESS.getCode()){
            throw new BizException("调用cjg远程服务新增方法信息返回失败！");
        }
        return result.getModel();
    }


    public Boolean removeMethod(InterfaceManage interfaceManage, MethodManage methodManage,String operator){
        //String operator = UserSessionLocal.getUser().getUserId();
        MethodDeleteReq req = new MethodDeleteReq();
        req.setMethodName(methodManage.getMethodCode());
        req.setAppCode(interfaceManage.getCjgAppId());
        req.setInterfaceName(interfaceManage.getServiceCode());
        req.setModifyBy(operator);
        Result<Boolean> result = null;
        try {
            log.info("busInterfaceRpcService.deleteMethod request={} ", req);
            result = busAppInterfaceRpcService.deleteMethod(req);
            log.info("busInterfaceRpcService.deleteMethod response={} ", JSON.toJSONString(result));
        } catch (Exception e) {
            log.error("busInterfaceRpcService.deleteMethod call occur exception>>>>>> ", e);
            throw new BizException("远程服务调用异常！");
        }
        if(result==null||result.getCode()!=StatusMessage.SUCCESS.getCode()){
            throw new BizException("调用cjg远程服务删除方法信息返回失败！");
        }
        return result.getModel();
    }


    public Set<String> getInterfaceMethodNames(InterfaceManage interfaceManage){
        MethodSearchReq req = new MethodSearchReq();
        req.setAppCode(interfaceManage.getCjgAppId());
        req.setInterfaceName(interfaceManage.getServiceCode());
        Result<List<MethodInfoVo>> result = null;
        try {
            log.info("busInterfaceRpcService.searchMethod request={} ", req);
            result = busAppInterfaceRpcService.searchMethod(req);
            log.info("busInterfaceRpcService.searchMethod response={} ", JSON.toJSONString(result));
        } catch (Exception e) {
            log.error("busInterfaceRpcService.searchMethod call occur exception>>>>>> ", e);
            throw new BizException("远程服务调用异常！");
        }
        if(result==null||result.getCode()!=StatusMessage.SUCCESS.getCode()){
            throw new BizException("调用cjg远程服务查询接口下方法信息返回失败！");
        }
        if(CollectionUtils.isEmpty(result.getModel()))return Collections.emptySet();
        return result.getModel().stream().map(o->o.getMethodName()).collect(Collectors.toSet());
    }


    public boolean createInterface(InterfaceCreateReq req){
        Result<Boolean> result = null;
        try {
            log.info("busInterfaceRpcService.createInterface request={} ", req);
            result = busAppInterfaceRpcService.createInterface(req);
            log.info("busInterfaceRpcService.createInterface response={} ", JSON.toJSONString(result));
        } catch (Exception e) {
            log.error("busInterfaceRpcService.createInterface call occur exception>>>>>> ", e);
            throw new BizException("远程服务调用异常！");
        }
        if(result==null||result.getCode()!=StatusMessage.SUCCESS.getCode()){
            throw new BizException("调用cjg远程服务创建接口返回失败！");
        }
        return BooleanUtils.isTrue(result.getModel());
    }

    public boolean isExistedInterface(String appCode,String interfaceName){
        Result<InterfaceDetailsVo> result = null;
        try {
            log.info("busInterfaceRpcService.getInterface request appCode={} interfaceName={}", appCode,interfaceName);
            result = busAppInterfaceRpcService.getInterface(appCode, interfaceName);
            log.info("busInterfaceRpcService.getInterface response={} ", JSON.toJSONString(result));
        } catch (Exception e) {
            log.error("busInterfaceRpcService.getInterface call occur exception>>>>>> ", e);
            throw new BizException("远程服务调用异常！");
        }
        if(result==null||result.getCode()!=StatusMessage.SUCCESS.getCode()){
            throw new BizException("调用cjg远程服务查询接口是否存在返回失败！");
        }
        return result.getModel()!=null;
    }


    public String getCjgAppCodeById(Integer cjgAppId){
        Result<ComponentInfoVo> result = busAppInterfaceRpcService.getAppById(cjgAppId);
        if(result==null||result.getCode()!=StatusMessage.SUCCESS.getCode() ){
            throw new BizException("调用cjg远程服务查询接口是否存在返回失败！");
        }
        if(result.getModel() == null) return null;
        return result.getModel().getAppCode();
    }
}
