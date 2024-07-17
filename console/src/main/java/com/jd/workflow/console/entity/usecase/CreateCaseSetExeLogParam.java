package com.jd.workflow.console.entity.usecase;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
@Data
@Accessors(chain = true)
public class CreateCaseSetExeLogParam {
    /**
     * jsf别名
     */
//    @NotNull(message = "属性jsfAlias不能为空")
    @Length(min = 1, max = 256,message = "属性jsfAlias长度，要在1-255个字符之间")
    private String jsfAlias;

    /**
     * IP+端口
     */
//    @NotNull(message = "属性ip不能为空")
    @Length(min = 1, max = 256,message = "属性ip长度，要在1-255个字符之间")
    private String ip;

    /**
     * 用例集ID
     */
    @NotNull(message = "属性caseSetId不能为空")
    @Min(value = 0L, message = "属性caseSetId需大于零")
    private Long caseSetId;

    /**
     * http环境信息
     */
    private String httpEnv;

}
