package com.jd.workflow.console.listener.cjg.entity;

import lombok.ToString;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * 组件实体类
 */

@ToString
public class ComponentInfoEntity implements Serializable {
    private int id;//编号
    private String name;//组件名称
    private String cname;//组件名称
    private String deptName;//组件所属部门
    private Integer compontentTypeId;//组件类型编号 1-应用  2-接口 3-方法  4-领域
    private String versionNo;//组件版本
    private String desc;//组件说明
    private Integer relId;//关联资源编号 根据component_type来决定关联appinterfacemethod
    private String protocol;//协议 jsf、HTTP等等
    private Integer producted;//是否支持产品空间
    private Integer stitchable;//是否支持流程编排
    private String extInfo;//扩展字段 json格式字符串
    private Integer compressFlag;//压缩标识，用于处理dbconfig的压缩属性，默认为0不压缩，1标识压缩
    private String createBy;//创建人编号
    private Timestamp createAt;//创建日期
    private String modifyBy;//修改者编号
    private Timestamp modifyAt;//修改日期
    private Byte yn;//逻辑删除 0无效 1有效
    private Integer weight;//权重
    private Integer trafficFlag; //是否为流量域应用 null/0其他应用，1流量域应用

    private String authLevel;//权限级别，0为接口，1为方法

    private Integer authCompressFlag;//权限压缩标识，用于处理dbconfig的压缩属性，默认为0不压缩，1标识压缩
    private String authdataCompressFlag;//权限压缩标识，用于处理dbconfig的压缩属性， 使用json存储

    private Integer source;

    // 标签ID ， 不与DB映射

    private List<Integer> tagIds;

    //private List<ComponentTagEntity> tags;

    private List<String> productErps;
    private Map<String, Object> productName;
    private String devLanguage;
 
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


   
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    
    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    
    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    
    public Integer getCompontentTypeId() {
        return compontentTypeId;
    }

    public void setCompontentTypeId(Integer compontentTypeId) {
        this.compontentTypeId = compontentTypeId;
    }

   
    public String getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(String versionNo) {
        this.versionNo = versionNo;
    }

    
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    
    public Integer getRelId() {
        return relId;
    }

    public void setRelId(Integer relId) {
        this.relId = relId;
    }

    
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    
    public Integer getProducted() {
        return producted;
    }

    public void setProducted(Integer producted) {
        this.producted = producted;
    }

   
    public Integer getStitchable() {
        return stitchable;
    }

    public void setStitchable(Integer stitchable) {
        this.stitchable = stitchable;
    }

     
    public String getExtInfo() {
        return extInfo;
    }

    public void setExtInfo(String extInfo) {
        this.extInfo = extInfo;
    }

     
    public Integer getCompressFlag() {
        return compressFlag;
    }

    public void setCompressFlag(Integer compressFlag) {
        this.compressFlag = compressFlag;
    }

     
    public Integer getAuthCompressFlag() {
        return authCompressFlag;
    }

    public void setAuthCompressFlag(Integer authCompressFlag) {
        this.authCompressFlag = authCompressFlag;
    }

    
    public String getAuthdataCompressFlag() {
        return authdataCompressFlag;
    }

    public void setAuthdataCompressFlag(String authdataCompressFlag) {
        this.authdataCompressFlag = authdataCompressFlag;
    }

   
    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

  
    public String getModifyBy() {
        return modifyBy;
    }

    public void setModifyBy(String modifyBy) {
        this.modifyBy = modifyBy;
    }

   
    public Timestamp getModifyAt() {
        return modifyAt;
    }

    public void setModifyAt(Timestamp modifyAt) {
        this.modifyAt = modifyAt;
    }

    

    public Byte getYn() {
        return yn;
    }

    public void setYn(Byte yn) {
        this.yn = yn;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    

    public Integer getTrafficFlag() {
        return trafficFlag;
    }

    public void setTrafficFlag(Integer trafficFlag) {
        this.trafficFlag = trafficFlag;
    }

    

    public String getAuthLevel() {
        return authLevel;
    }

    public void setAuthLevel(String authLevel) {
        this.authLevel = authLevel;
    }


    

    public Integer getSource() {
        return source;
    }

    public String getDevLanguage() {
        return devLanguage;
    }

    public void setDevLanguage(String devLanguage) {
        this.devLanguage = devLanguage;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

   
    public List<Integer> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Integer> tagIds) {
        this.tagIds = tagIds;
    }


   


   
    public List<String> getProductErps() {
        return productErps;
    }

    public void setProductErps(List<String> productErps) {
        this.productErps = productErps;
    }

    
    public Map<String, Object> getProductName() {
        return productName;
    }

    public void setProductName(Map<String, Object> productName) {
        this.productName = productName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ComponentInfoEntity that = (ComponentInfoEntity) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(name, that.name)
                .append(cname, that.cname)
                .append(deptName,that.deptName)
                .append(compontentTypeId, that.compontentTypeId)
                .append(versionNo, that.versionNo)
                .append(desc, that.desc)
                .append(relId, that.relId)
                .append(protocol, that.protocol)
                .append(extInfo, that.extInfo)
                .append(createBy, that.createBy)
                .append(createAt, that.createAt)
                .append(modifyBy, that.modifyBy)
                .append(modifyAt, that.modifyAt)
                .append(yn, that.yn)
                .append(weight, that.weight)
                .append(stitchable, that.stitchable)
                .append(producted, that.producted)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(cname)
                .append(deptName)
                .append(compontentTypeId)
                .append(versionNo)
                .append(desc)
                .append(relId)
                .append(protocol)
                .append(extInfo)
                .append(createBy)
                .append(createAt)
                .append(modifyBy)
                .append(modifyAt)
                .append(yn)
                .append(weight)
                .append(producted)
                .append(stitchable)
                .toHashCode();
    }


}
