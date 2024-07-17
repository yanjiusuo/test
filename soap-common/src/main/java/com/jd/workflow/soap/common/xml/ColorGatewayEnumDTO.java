package com.jd.workflow.soap.common.xml;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/14
 */

import com.jd.workflow.soap.common.xml.schema.JsonEnumPropDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColorGatewayEnumDTO {

    private String name;

    private Integer value;


    public static ColorGatewayEnumDTO getSourceInstance(Integer source) {
        ColorGatewayEnumDTO dto = new ColorGatewayEnumDTO();
        if (null != source) {
            dto.setValue(source);
            dto.setName(source == 1 ? "网关生成" : (source == 2 ? "客户端传递" : "未知source"));
        }
        return dto;
    }

    /**
     * 1-传递 0-不传
     * @param transparent
     * @return
     */
    public static ColorGatewayEnumDTO getSimpleYesOrNo(Integer transparent) {
        ColorGatewayEnumDTO dto = new ColorGatewayEnumDTO();
        if (null != transparent) {
            dto.setName(transparent == 1 ? "是" : "否");
            dto.setValue(transparent);
        }else{
            //如果是空，按照0处理，便于预发线上比较数据
            dto.setName("否");
            dto.setValue(0);
        }
        return dto;
    }

    /**
     * 1-网关规范 2-用户自定义 3-http协议
     * @param transparent
     * @return
     */
    public static ColorGatewayEnumDTO getMark(Integer transparent) {
        ColorGatewayEnumDTO dto = new ColorGatewayEnumDTO();
        if (null != transparent) {
            dto.setValue(transparent);
            dto.setName(transparent == 1 ? "网关规范" : (transparent == 3 ? "http协议" : (transparent == 2 ? "用户自定义" : "其他")));
        }
        return dto;
    }

}
