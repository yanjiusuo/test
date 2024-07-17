package com.jd.workflow.console.elastic.entity;

import com.jd.common.util.StringUtils;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
//import org.springframework.data.annotation.Id;
/*import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;*/

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
@Document(indexName = ElasticConstants.INTERFACE_INDEX_ALIAS, type = "_doc")
public class InterfaceManageDoc implements Serializable {

    /**
     * Id - 当前的属性，对应Elasticsearch中索引的_id元数据。代表主键。
     * 且，当前属性会在Elasticsearch保存的数据结构中。{"id":"", "title":"", "remark":""}
     */
    @Id
    private String id; // 主键

    private Long interfaceId;
    private String name;
    private Integer visibility;

    private Integer type;

    /**
     * 成员信息，以空格分割
     */
    private String erps;
    /**
     * 负责人
     */
    private String adminCode;

    private String serviceCode;
    private String appName;
    private String appCode;
    private String deptName;
    private String cloudFileTags;
    private Long appId;

    public static InterfaceManageDoc from(InterfaceManage manage, AppInfo appInfo, Set<String> members) {
        InterfaceManageDoc doc = new InterfaceManageDoc();
        BeanUtils.copyProperties(manage, doc);
        doc.setId(String.valueOf(manage.getId()));
        doc.setAppCode(appInfo.getAppCode());
        doc.setAppName(appInfo.getAppName());
        doc.setInterfaceId(manage.getId());
        doc.setType(manage.getType());
        doc.setCloudFileTags(manage.getCloudFileTags());
        doc.setAppId(manage.getAppId());
        AppInfoDTO dto = new AppInfoDTO();
        List<String> list = dto.splitMembers(appInfo.getMembers());
        if (!ObjectHelper.isEmpty(dto.getOwner())) {
            doc.setAdminCode(dto.getOwner().get(0));
        }
        doc.setErps(StringHelper.join(members, " "));
        return doc;
    }
}
