package com.jd.workflow.console.dto.requirement;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class WfFlowVo {

    private Long id;

    /**
     * 需求名称
     *
     * @required true
     */
    @Length(max=500,message = "需求名称字符长度超过500字符")
    private String name;

    /**
     * 需求code
     *
     * @required true
     */
    @Length(max=500,message = "需求code字符长度超过500字符")
    @NotBlank(message = "需求code不能为空")
    private String code;
    /**
     * 模板code
     *
     * @required true
     */
    @Length(max=500,message = "模板code字符长度超过500字符")
    @NotBlank(message = "模板code不能为空")
    private String templateCode;


    /**
     * 透传
     */
    private  Long parentId;
    /**
     * 下一点数据List
     *
     * @required true
     */
    @NotEmpty(message = "下一点数据List不能为空")
    private List<SaveNodeDetailVo> nodeDetailVos;
    @Data
   public static class SaveNodeDetailVo {

        /**
         * 节点详情id
         * @required true
         */
        private long detailId;
        /**
         * ext 数据
         * @required true
         */
        private String extDataStr;
        /**
         * 组件编码
         * @required true
         */
        private String componentCode;

    }
}
