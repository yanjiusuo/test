package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 接口空间类型
 *
 * 相关配置
 * http://taishan.jd.com/ducc/web/nswork?nsId=9570&nsName=up_portal_config&cId=54858&cName=config&envId=82413&envName=prd&defAppId=9115&dataType=0
 * @date: 2022/5/12 18:23
 * @author wubaizhao1
 */
@AllArgsConstructor
public enum InterfaceSpaceTypeEnum {
	COMMON(1, "通用"),
	PROJ(2, "项目协作"),
	DEVOPTOOL(3, "开发工具"),
	BUSINESS(4, "业务支撑"),
	DATAANALYS(5, "数据分析"),
	TEST(6, "测试运维"),
	OTHER(7, "其他"),
	;
	/**
	 * @date: 2022/5/12 18:23
	 * @author wubaizhao1 扩展点
	 */
	@Getter
	@Setter
	private Integer code;

	/**
	 * 描述
	 * @date: 2022/5/12 18:25
	 * @author wubaizhao1
	 */
	@Getter
	@Setter
	private String desc;


	/**
	 * @date: 2022/5/19 15:55
	 * @author wubaizhao1
	 * @param code
	 * @return
	 */
	public static InterfaceSpaceTypeEnum getByCode(Integer code){
		for (InterfaceSpaceTypeEnum value : InterfaceSpaceTypeEnum.values()) {
			if(value.getCode().equals(code)){
				return value;
			}
		}
		return null;
	}

	/**
	 *
	 * @param typeName
	 * @return
	 */
	public static InterfaceSpaceTypeEnum getByName(String typeName){
		String name=typeName.trim().toLowerCase();
		for (InterfaceSpaceTypeEnum value : InterfaceSpaceTypeEnum.values()) {
			if(value.getDesc().equals(name)){
				return value;
			}
		}
		return null;
	}
}
