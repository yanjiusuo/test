package com.jd.workflow.server.dto.requirement;

/**
 * @description:
 * 通过coding地址和分支名查询需求code
 * @author: zhaojingchun
 * @Date: 2024/5/30
 */
public class QueryRequirementCodeParam {
    /**
     * git@coding.jd.com:transform/data-transform.git
     * https://coding.jd.com/transform/data-transform.git
     * 只保留后半段：transform/data-transform.git
     */
    private String codingAddress;

    /**
     * 分支名
     */
    private String branchName;

    public String getCodingAddress() {
        return codingAddress;
    }

    public void setCodingAddress(String codingAddress) {
        this.codingAddress = codingAddress;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}
