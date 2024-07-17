package com.jd.workflow.console.elastic.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;

@Data
@Document(indexName = ElasticConstants.METHOD_INDEX_ALIAS, type = "_doc")
public class MethodManageDoc implements Serializable {

    /**
     * Id - 当前的属性，对应Elasticsearch中索引的_id元数据。代表主键。
     * 且，当前属性会在Elasticsearch保存的数据结构中。{"id":"", "title":"", "remark":""}
     */
    @Id
    private String id; // 主键

    private Long methodId;
    private Integer visibility;
    private String name;
    Integer type;
    String methodCode;
    String docInfo;
    Long interfaceId;
    String content;
    String path;
    private String serviceCode;
    private String serviceName;
    private Long appId;
    /**
     * JAPI应用中文名
     */
    private String appName;
    /**
     * 应用code
     */
    private String appCode;
    private String deptName;
}
