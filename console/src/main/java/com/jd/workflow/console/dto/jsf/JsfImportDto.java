package com.jd.workflow.console.dto.jsf;

import com.jd.workflow.console.dto.doc.InterfaceDocConfig;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class JsfImportDto {
    Long id;
    /**
     * jsf中文名
     */
   // @NotNull(message = "接口id不可为空")
    String interfaceId;
    /**
     * jsf英文名
     */
    String serviceCode;
    Long appId;
    //@NotNull(message = "接口负责人不可为空")
    String adminCode;
    // @NotNull(message = "groupId不可为空")
    String groupId;
    // @NotNull(message = "artifactId不可为空")
    String artifactId;
    //  @NotNull(message = "artifactId不可为空")
    String version;
    //@NotNull(message = "接口描述不可为空")
    String docInfo;

    InterfaceDocConfig docConfig;

    Integer visibility;

    Integer level;
    /**
     * 是否跳过回调listener，批量导入jsf接口的时候会创建
     * mock数据，导致导入效率较低，这块可以通过跳过这一块优化一下哈
     */
    Boolean skipListener = false;

    String functionId;

    public boolean skipListener(){
        return skipListener != null && skipListener == true;
    }
}
