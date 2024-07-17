package com.jd.workflow.console.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Objects;

/**
 * 导入藏经阁应用
 * @author xiaobei
 * @date 2023-02-26 15:16
 */
@Getter
@Setter
@ToString
public class AppImportDTO implements Serializable {

    private static final long serialVersionUID = -1694625830329737811L;

    /**
     * 应用名称
     */
    @ExcelProperty(value = "*应用名称")
    private String appName;

    /**
     * 应用编码
     */
    @ExcelProperty(value = "*应用编码")
    private String appCode;

    /**
     * 研发负责人
     */
    @ExcelProperty(value = "*研发负责人")
    private String owner;

    /**
     * 产品负责人
     */
    @ExcelProperty(value = "*产品负责人")
    private String productor;

    /**
     * 测试负责人
     */
    @ExcelProperty(value = "*测试负责人")
    private String tester;

    /**
     * 接口鉴权级别（0-接口级，1-方法级）
     */
    @ExcelProperty(value = "*接口鉴权级别（0-接口级，1-方法级）")
    private String authLevel;

    /**
     * 导入失败原因
     */
    private String failMsg;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppImportDTO that = (AppImportDTO) o;
        return Objects.equals(appName, that.appName) && Objects.equals(appCode, that.appCode) && Objects.equals(owner, that.owner) && Objects.equals(productor, that.productor) && Objects.equals(tester, that.tester) && Objects.equals(authLevel, that.authLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, appCode, owner, productor, tester, authLevel);
    }
}
