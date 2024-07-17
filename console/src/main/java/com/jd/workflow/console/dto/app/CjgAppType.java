package com.jd.workflow.console.dto.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jd.cjg.bus.request.AppTypeReq;
import com.jd.cjg.bus.vo.AppTypeVo;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
@Data
public class CjgAppType  {
    private Integer id;
    private String appCode;
    private Integer componentId;
    private Integer appType;
    @JsonProperty("pLevel")
    private String pLevel;
    @JsonProperty("pLevelNextDate")
    private Date pLevelNextDate;
    @JsonProperty("pLevelNextTwoDate")
    private Date pLevelNextTwoDate;


    public static CjgAppType from(AppTypeVo vo){
        CjgAppType cjgAppType = new CjgAppType();
        BeanUtils.copyProperties(vo,cjgAppType);
        return cjgAppType;
    }
    public AppTypeReq toReq(){
        AppTypeReq appTypeReq = new AppTypeReq();
        BeanUtils.copyProperties(this,appTypeReq);
        return appTypeReq;
    }

    public static void main(String[] args) {
        String data = "{\"appName\":\"wjfRRR\",\"addType\":\"direct\",\"currentLimitLevel\":\"0\",\"appCName\":\"wjfRRR\",\"description\":\"wjfRRR\",\"projectManager\":\"wangjingfang3\",\"productManagers\":[\"tangqianqian11\"],\"testers\":[\"ext.chenweixiang1\"],\"devLanguage\":\"JAVA\",\"appTypeEntities\":[{\"appType\":1,\"pLevel\":\"L0\",\"pLevelNextDate\":\"2023-07-06 10:49:56\",\"pLevelNextTwoDate\":\"2023-07-01 10:49:58\"}]}";
        final CjgAppDto cjgAppDto = JsonUtils.parse(data, CjgAppDto.class);
        System.out.println(cjgAppDto );
    }

}
