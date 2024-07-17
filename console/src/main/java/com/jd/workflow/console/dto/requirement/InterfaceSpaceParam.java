package com.jd.workflow.console.dto.requirement;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/1
 */

import com.jd.workflow.console.base.PageParam;
import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/1
 */
@Data
public class InterfaceSpaceParam extends PageParam {

    /**
     * 模糊查询
     */
    private String spaceName;

    /**
     * 接口市场名称
     */
    private String openSolutionSpaceName;
    /**
     * 开放类型  InterfaceSpaceTypeEnum
     * COMMON(1, "通用"),
     * 	PROJ(2, "项目协作"),
     * 	DEVOPTOOL(3, "开发工具"),
     * 	BUSINESS(4, "业务支撑"),
     * 	DATAANALYS(5, "数据分析"),
     * 	TEST(6, "测试运维"),
     * 	OTHER(7, "其他"),
     */
    private String openType;
    /**
     * 部门 举例 /00000000/00013807/00024935/00010279/00055367/00117411
     */
    private String departmentId;
    /**
     * 负责人
     */
    private String admin;
    /**
     *  http=1 jsf=3
     */
    private Integer interfaceType;


}
