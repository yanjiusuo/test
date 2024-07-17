package com.jd.workflow.console.service.impl;

import com.jd.cjg.component.ComponentProvider;
import com.jd.cjg.component.dto.CreateComponentRequest;
import com.jd.cjg.component.vo.ComponentVo;
import com.jd.cjg.result.CjgResult;
import com.jd.common.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dto.AppImportDTO;
import com.jd.workflow.console.dto.AppImportResultDTO;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.helper.CjgHelper;
import com.jd.workflow.console.service.AppImportService;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 导入藏经阁应用
 * @author xiaobei
 * @date 2023-02-26 17:03
 */
@Slf4j
@Service("appImportService")
public class AppImportServiceImpl implements AppImportService {

    private ComponentProvider componentProvider;

    @Autowired
    IAppInfoService appInfoService;
    @Autowired
    CjgHelper cjgHelper;

    @Autowired
    public void setComponentProvider(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public AppImportResultDTO batchImportCjgApp(List<AppImportDTO> authApplyList, String currentUser) {
        AppImportResultDTO appImportResultDTO = new AppImportResultDTO();
        if(CollectionUtils.isEmpty(authApplyList)) {
            appImportResultDTO.setSuccessList(Collections.emptyList());
            appImportResultDTO.setFailList(Collections.emptyList());
            return appImportResultDTO;
        }
        List<AppImportDTO> successList = new ArrayList<>(authApplyList.size());
        List<AppImportDTO> failList = new ArrayList<>(8);
        appImportResultDTO.setSuccessList(successList);
        appImportResultDTO.setFailList(failList);
        // 1. 过滤出空的应用信息
        List<AppImportDTO> emptyAppList = authApplyList.stream()
                .filter(app -> ObjectUtils.isEmpty(app.getAppName())
                        || ObjectUtils.isEmpty(app.getAppName())
                        || ObjectUtils.isEmpty(app.getOwner())
                        || ObjectUtils.isEmpty(app.getProductor())
                        || ObjectUtils.isEmpty(app.getTester())
                        || ObjectUtils.isEmpty(app.getAuthLevel()))
                .peek(app -> app.setFailMsg("必填字段为空"))
                .collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(emptyAppList)) {
            // 将不全的应用过滤出来
            authApplyList.removeAll(emptyAppList);
            failList.addAll(emptyAppList);
        }
        // 2. 过滤出接口鉴权格式错误的数据
        List<AppImportDTO> formatErrorList = authApplyList.stream()
                .filter(app -> (!"0".equals(app.getAuthLevel()) && !"1".equals(app.getAuthLevel())))
                .peek(app -> app.setFailMsg("接口鉴权级别只能是 0 或 1"))
                .collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(formatErrorList)) {
            // 将格式错误的数据应用过滤出来
            authApplyList.removeAll(formatErrorList);
            failList.addAll(formatErrorList);
        }
        // 3. 超长内容
        List<AppImportDTO> tooLongContentList = authApplyList.stream()
                .filter(app -> app.getAppName().length() > 200 || app.getAppCode().length() > 100)
                .peek(app -> app.setFailMsg("应用名称或code超长"))
                .collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(tooLongContentList)) {
            // 将超长内容的应用过滤出来
            authApplyList.removeAll(tooLongContentList);
            failList.addAll(tooLongContentList);
        }
        // 负责人信息是不正常
        List<AppImportDTO> tooManyManagerList = authApplyList.stream()
                .filter(app -> app.getOwner().contains(",")
                        || app.getProductor().contains(",")
                        || app.getTester().contains(","))
                .peek(app -> app.setFailMsg("负责人仅允许一个"))
                .collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(tooManyManagerList)) {
            // 将负责人信息是不正常的应用过滤出来
            authApplyList.removeAll(tooManyManagerList);
            failList.addAll(tooManyManagerList);
        }
        for (AppImportDTO appImportDTO : authApplyList) {
            CreateComponentRequest request = new CreateComponentRequest();
            request.setAppName(appImportDTO.getAppCode());
            request.setAppCName(appImportDTO.getAppName());
            request.setCreateBy(currentUser);
            request.setProjectManager(appImportDTO.getOwner());
            request.setProductManagers(Collections.singletonList(appImportDTO.getProductor()));
            request.setTesters(Collections.singletonList(appImportDTO.getTester()));
            request.setCurrentLimitLevel(appImportDTO.getAuthLevel());


            try {
               // CjgResult<ComponentVo> createComponentResult = componentProvider.createComponent(request);
                appInfoService.addApp(toApp(appImportDTO));
                successList.add(appImportDTO);
               /* String msg = null;
                if(createComponentResult.checkSuccess()) {
                    successList.add(appImportDTO);
                } else if(!ObjectUtils.isEmpty(msg = createComponentResult.getMessage())
                        && (msg.contains("已存在") || msg.contains("曾在藏经阁创建"))) {
                    appImportDTO.setFailMsg("应用编码已存在");
                    failList.add(appImportDTO);
                } else {
                    appImportDTO.setFailMsg("其他");
                    failList.add(appImportDTO);
                }*/
                log.error("应用code：{},jsf请求结果为：{}", appImportDTO.getAppCode(), true);
            } catch (BizException e){
                log.error("app.err_import_app:appCode={}",appImportDTO,e);
                failList.add(appImportDTO);
                appImportDTO.setFailMsg(e.getMsg());
            }catch (Exception e) {
                appImportDTO.setFailMsg("其他");
                failList.add(appImportDTO);
                e.printStackTrace();
                log.error("app.err_import_app:appCode={}",appImportDTO.getAppCode(),e);
            }
        }
        return appImportResultDTO;
    }

    AppInfoDTO  toApp(AppImportDTO request){
        AppInfoDTO dto = new AppInfoDTO();
        dto.setAppName(request.getAppName());
        dto.setAppCode(request.getAppCode());
        AppInfoDTO existedApp = cjgHelper.getCjgComponetInfoByCode(dto.getAppCode());
        if(existedApp != null){
            dto.setCjgAppId(request.getAppCode());
        }
        dto.setOwner(Collections.singletonList(request.getOwner()));
        if(StringUtils.isNotBlank(request.getProductor())){
            dto.setProductor(StringHelper.split(request.getProductor(),","));
        }
        if(StringUtils.isNotBlank(request.getTester())){
            dto.setTester(StringHelper.split(request.getTester(),","));
        }
        dto.setAuthLevel(request.getAuthLevel());
        dto.setTenantId(UserSessionLocal.getUser().getTenantId());
        return dto;
    }
}
