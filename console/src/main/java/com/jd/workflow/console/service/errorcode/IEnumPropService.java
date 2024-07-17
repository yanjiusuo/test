package com.jd.workflow.console.service.errorcode;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/4
 */

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.doc.EnumClassDTO;
import com.jd.workflow.console.dto.errorcode.DeleteEnumPropDTO;
import com.jd.workflow.console.dto.errorcode.EnumDTO;
import com.jd.workflow.console.dto.errorcode.EnumPropDTO;
import com.jd.workflow.console.dto.errorcode.SaveEnumDTO;
import com.jd.workflow.console.entity.errorcode.EnumProp;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/4
 */
public interface IEnumPropService extends IService<EnumProp> {

    /**
     * 查询错误码属性
     *
     * @param appId
     * @return
     */
    List<EnumPropDTO> queryErrorCodeProp(Long appId);


    /**
     * 保存错误码属性
     *
     * @param saveEnumDTO
     * @return
     */
    Boolean saveEnumProps(SaveEnumDTO saveEnumDTO);

    /**
     * 查询枚举属性
     *
     * @param appId
     * @param enumId
     * @return
     */
    List<EnumPropDTO> queryEnumProp(Long appId, Long enumId);

    /**
     * 上报接口
     *
     * @param enums
     * @return
     */
    Boolean saveEnums(List<EnumClassDTO> enums, Long appId);


    /**
     * 删除枚举值
     *
     * @param enumPropDTO
     * @return
     */
    Boolean deleteEnumProp(EnumPropDTO enumPropDTO);

    /**
     * 批量删除
     * @param deleteEnumPropDTO
     * @return
     */
    Boolean deleteEnumProps( DeleteEnumPropDTO deleteEnumPropDTO);

}
