package com.jd.workflow.console.dto.share;

import com.jd.workflow.console.dto.InterfaceShareTreeDTO;
import com.jd.workflow.console.dto.InterfaceShareTreeModel;
import lombok.Data;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/31 14:37
 * @Description:
 */
@Data
public class AppendShareDTO {

    /**
     * 分享组id
     */
    private Long  id;


    /**
     * 追加的分享树
     */
    private InterfaceShareTreeModel treeModel;


}
