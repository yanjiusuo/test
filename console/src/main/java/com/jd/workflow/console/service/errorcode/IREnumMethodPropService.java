package com.jd.workflow.console.service.errorcode;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/4
 */

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.errorcode.BindPropParam;
import com.jd.workflow.console.entity.errorcode.REnumMethodProp;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/4
 */
public interface IREnumMethodPropService extends IService<REnumMethodProp> {
    /**
     * 绑定属性与枚举关系
     * @param bindPropParam
     * @return
     */
    Boolean bindEnum(BindPropParam bindPropParam);

    /**
     * 获取绑定的属性列表
     * @param bindPropParam
     * @return
     */
    List<REnumMethodProp> bindEnumList(BindPropParam bindPropParam);

    /**
     * 删除绑定关系
     * @param bindPropParam
     * @return
     */
    Boolean deleteBindEnum( BindPropParam bindPropParam);

}
