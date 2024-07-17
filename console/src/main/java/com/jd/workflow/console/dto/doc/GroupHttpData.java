package com.jd.workflow.console.dto.doc;

import com.jd.workflow.console.dto.HttpMethodModel;
import lombok.Data;

import java.util.List;

@Data
public class GroupHttpData<T> {
    /**
     * 分组名称
     */
    String groupName;
    /**
     * 分组描述
     */
    String groupDesc;
    /**
     * http接口数据
     */
    List<T> httpData;
}
