package com.jd.workflow.console.dto.doc;

import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.entity.MethodManage;
import lombok.Data;
import org.springframework.core.type.ClassMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class DocReportDto {
    /**
     *应用code
     */
    String appCode;
    /**
     * 密钥
     */
    String appSecret;
    //String jsf;
    /**
     * 上报ip
     */
    String ip;
    //List<ClassMetadata> jsfDocs;
    /**
     * jsf文档
     */
    String jsfDocs;
    /**
     * swagger全量接口字符串
     */
    String swagger;

    private Boolean sync;

    String jdosAppCode;

    /**
     * swagger部分接口数据
     */
    Map<String, List<HttpMethodModel>> httpData;
    /**
     * 分组http数据，在线联调自己定义的http格式。swagger接口没有存类名、泛型等信息，生成sdk的时候不够准确，在线联调采集的时候可以弥补这个问题
     */
    List<GroupHttpData<HttpMethodModel>> groupHttpData;

    List<ApiClassModel> models;
    /**
     * swagger导入接口数据
     */
    Map<String, List<MethodManage>> importData;
    /**
     * 项目code
     */
    String httpAppCode;
    /**
     * 接口id
     */
    Long interfaceId;


    /**
     * 创建日期：历史数据导入的时候可能会需要根据创建日期导入。创建日期的格式为：
     * yyyy-MM-dd HH:mm:ss
     */
    String createDate;

    /**
     * 接口文档上报人
     */
    String erp;

    /**
     * 行云需求code
     */
    String requireCode;

    /**
     * 通知人列表，分号分隔
     */
    String members;

    /**
     * 上报数据是否覆盖页面更改数据 1=覆盖
     */
    Integer isCover;

    /**
     * 是否自动上报：1-是 0-否  2- 自动导入jsf平台的
     */
    Integer autoReport;

    public void init(){
        if(httpData != null && !httpData.isEmpty()){
            groupHttpData = new ArrayList<>();
            for (Map.Entry<String, List<HttpMethodModel>> entry : httpData.entrySet()) {
                GroupHttpData<HttpMethodModel> data = new GroupHttpData<>();
                data.setGroupDesc(entry.getKey());
                data.setHttpData(entry.getValue());
                groupHttpData.add(data);
            }
        }
    }

    private String codeRepository;
    private String branch;
    private String channel;
}
