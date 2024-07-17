package com.jd.workflow.console.dto.app;

import com.jd.cjg.bus.request.AppTypeReq;
import com.jd.cjg.bus.request.ComponentCreateReq;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.soap.common.util.ObjectHelper;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {"addType":"direct","appName":"fff","appCName":"sss","description":"tttxxx","projectManager":"wangjingfang3","developers":["tangqianqian11"],"productManagers":["wangjingfang3"],"testers":["wangjingfang3"],"tesErpListRelation":["zhaojingchun"],"currentLimitLevel":"0","devLanguage":"Go","appTypeEntities":[{"appType":4,"pLevel":"L0","pLevelNextDate":"","pLevelNextTwoDate":""}],"architects":[],"versionNo":""}
 */
@Data
public class CjgAppDto {
    Long id;
    String addType;
    /**
     * 应用中文名
     */
    String appCName;
    /**
     * 应用名称
     */
    String appName;
    List<CjgAppType> appTypeEntities;

    List<String> architects;
    /**
     * 调用级别 0 接口 1方法
     */
    String currentLimitLevel;
    /**
     * 描述
     */
    String description;
    /**
     * 研发人员
     */
    List<String> developers;
    /**
     * 开发语言
     */
    String devLanguage;
    /**
     * 产品负责人
     */
    List<String> productManagers;
    /**
     * 项目负责人
     */
    String projectManager;
    /**
     * 测试相关人员
     */
    List<String> testErpListRelation;
    /**
     * jdos应用系统信息
     */
    private ComponentCreateReq.JdosAppSysInfo jdosAppSysInfo;
    /**
     * 测试负责人
     */
    List<String> testers;
    String versionNo;
    public AppInfoDTO toAppInfoDto(){
        AppInfoDTO appInfoDTO = new AppInfoDTO();
        appInfoDTO.setId(this.id);
        appInfoDTO.setAppCode(this.appName);
        appInfoDTO.setAppName(this.appCName);
        appInfoDTO.setDesc(this.description);
        appInfoDTO.setProductor(this.productManagers);
        appInfoDTO.setOwner(Collections.singletonList(this.projectManager));
        appInfoDTO.setAuthLevel(currentLimitLevel);
        appInfoDTO.setMember(this.developers);
        //appInfoDTO.setAppTypeEntities(this.appTypeEntities);
        appInfoDTO.setTester(this.testers);

            appInfoDTO.setAppTypeReqList(this.appTypeEntities);


        appInfoDTO.setDevLanguage(this.devLanguage);
        appInfoDTO.setTestMember(this.testErpListRelation);
        /*appInfoDTO.setArchitects(this.architects);
        appInfoDTO.setDevLanguage(this.devLanguage);
        appInfoDTO.setCurrentLimitLevel(this.currentLimitLevel);
        appInfoDTO.setVersionNo(this.versionNo);*/
        return appInfoDTO;
    }
    public static CjgAppDto newCjgAppDto(AppInfoDTO dto){
        CjgAppDto cjgAppDto = new CjgAppDto();
        cjgAppDto.setId(dto.getId());
        cjgAppDto.setAppCName(dto.getAppName());
        cjgAppDto.setAppName(dto.getAppCode());
        cjgAppDto.setCurrentLimitLevel(dto.getAuthLevel());
        cjgAppDto.setAppTypeEntities(dto.getAppTypeReqList());
        cjgAppDto.setDevLanguage(dto.getDevLanguage());
        cjgAppDto.setDescription(dto.getDesc());
        cjgAppDto.setProductManagers(dto.getProductor());
        if(!ObjectHelper.isEmpty(dto.getOwner())){
            cjgAppDto.setProjectManager(dto.getOwner().get(0));
        }else{

        }

        cjgAppDto.setDevelopers(dto.getMember());
        cjgAppDto.setTesters(dto.getTester());
        cjgAppDto.setTestErpListRelation(dto.getTestMember());
        return cjgAppDto;
    }
}
