package com.jd.workflow.console.dto.version;

import com.jd.workflow.console.dto.MethodGroupTreeModel;
import com.jd.workflow.console.entity.doc.InterfaceVersion;
import lombok.Data;

/**
 * 比较版本结构体
 */
@Data
public class CompareVersionDTO {

    private InterfaceVersion baseVersion;

    private InterfaceVersion compareVersion;

    private MethodGroupTreeModel treeModel;

    private Integer addCnt;

    private Integer updateCnt;

    private Integer delCnt;

    public void initModifyCnt(Integer addCnt,Integer updateCnt,Integer delCnt){
        this.addCnt = addCnt;
        this.updateCnt = addCnt;
        this.delCnt = delCnt;
    }

}
