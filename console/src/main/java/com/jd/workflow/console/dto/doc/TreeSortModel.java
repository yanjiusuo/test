package com.jd.workflow.console.dto.doc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MethodSortModel.class, name = "1"),
        @JsonSubTypes.Type(value = GroupSortModel.class, name = "2"),
        @JsonSubTypes.Type(value = InterfaceSortModel.class, name = "3"),
        @JsonSubTypes.Type(value = InterfaceSortModel.class, name = "4"),
        @JsonSubTypes.Type(value = InterfaceSortModel.class, name = "5")
})
@Data
public abstract class TreeSortModel {

    public static final String TYPE_METHOD = "1";
    public static final String TYPE_GROUP = "2";
    /**
     * model类型为接口
     */
    public static final String TYPE_INTERFACE = "3";
    /**
     * model类型为JSF接口分组
     */
    public static final String TYPE_INTERFACE_GROUP_JSF = "4";
    /**
     * model类型为HTTP接口分组
     */
    public static final String TYPE_INTERFACE_GROUP_HTTP = "5";
    private String key = UUID.randomUUID().toString();
    /**
     * 树节点id
     */
    private Long id;
    /**
     * 节点类型：1-接口(叶子节点) 2-文件夹(非叶子节点) 3-项目(非叶子节点)。
     */
    private String type;
    //private Integer order;
    /**
     * 接口路径
     */
    private String path;
    /**
     * 分组名称
     */
    private String name;
    /**
     * 分组英文名
     *
     */
    private String enName;
    /**
     * 应用id
     */
    private Long appId;
    /**
     * 接口类型
     * {@link com.jd.workflow.console.base.enums.InterfaceTypeEnum}
     */
    private Integer interfaceType;
    /**
     * 接口id,冗余存储
     */
    private Long interfaceId;


    private Boolean isColor;

    //1 add 2 updtate 3 delete 0 noChange
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String opType;

    public Long getId() {
        return id;
    }

    /**
     * 版本，导出文档使用，非导出文档不需要
     * @hidden
     */
    @JsonIgnore
    private String version;


    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOpType() {
        return opType;
    }

    public void setOpType(String opType) {
        this.opType = opType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String initKey(){
        setKey(getKey(getType(),interfaceId,getInterfaceType(), getId()));
        return key;
    }
    public static String getKey(String type,Long interfaceId, Integer interfaceType, Long id){
        return type+"_"+interfaceId+"_"+interfaceType+"_"+id;
    }
    public TreeSortModel clone(){
        TreeSortModel result = null;
        if(TYPE_METHOD.equals(type)){
            result = new MethodSortModel();
        }else if(TYPE_GROUP.equals(type)){
            result = new GroupSortModel();
        }else {
            result = new InterfaceSortModel();
        }

        BeanUtils.copyProperties(this,result);
        if(result instanceof GroupSortModel){
            GroupSortModel group = (GroupSortModel) this;
            if(group.getChildren() != null){
                ((GroupSortModel) result).setChildren(
                        group.getChildren().stream().map(item->item.clone()).collect(Collectors.toList())
                );
            }
        }
        return result;
    }
    public static void main(String[] args) {
        String code = "{\"interfaceId\":343,\"groupLastVersion\":\"20221118143031733\",\"treeModel\":{\"treeItems\":[{\"id\":2,\"name\":\"分组1\",\"children\":[{\"id\":1632,\"name\":\"saveConfigFile\",\"type\":\"3\"},{\"id\":1633,\"name\":\"initApp\",\"type\":\"1\"},{\"id\":1634,\"name\":\"microAppOverview\",\"type\":\"1\"},{\"id\":1635,\"name\":\"addMicroApp\",\"type\":\"1\"},{\"id\":1636,\"name\":\"appBuild\",\"type\":\"1\"},{\"id\":1637,\"name\":\"buildMicroApp\",\"type\":\"1\"},{\"id\":1638,\"name\":\"getBuildStatus\",\"type\":\"1\"},{\"id\":1639,\"name\":\"listMicroApps\",\"type\":\"1\"},{\"id\":1640,\"name\":\"listIosCert\",\"type\":\"1\"},{\"id\":1624,\"name\":\"addAppVersionPlan\",\"type\":\"1\"},{\"id\":1625,\"name\":\"listAppIntegration\",\"type\":\"1\"},{\"id\":1626,\"name\":\"addIntegrationRecord\",\"type\":\"1\"},{\"id\":1627,\"name\":\"getMicroAppKeyById\",\"type\":\"1\"},{\"id\":1628,\"name\":\"createVMSVersionPlan\",\"type\":\"1\"},{\"id\":1629,\"name\":\"listMicroAppByAppKey\",\"type\":\"1\"},{\"id\":1630,\"name\":\"countMicroApp\",\"type\":\"1\"},{\"id\":1631,\"name\":\"createApp\",\"type\":\"1\"}],\"type\":\"2\"}]}}";
        MethodGroupTreeDTO dto = JsonUtils.parse(code, MethodGroupTreeDTO.class);
        System.out.println(dto);
        dto.getTreeModel().getTreeItems().get(0).setOpType("1");
        System.out.println(JsonUtils.toJSONString(dto));
        dto = JsonUtils.parse(JsonUtils.toJSONString(dto), MethodGroupTreeDTO.class);
        System.out.println(dto.getTreeModel().getTreeItems().get(0).getOpType());
    }
}
