

# 表单

除了增删改查之外，经常会遇到需要校验表单，提交表单的需求。

下面是一个低代码完成表单提交需求的示例。它演示了如何在低代码中使用`公式`实现表单项之间的联动与表单的提交。

![image-20210820113425126](../../assets/img/example/form/image-20210820113425126.png)



## STEP1 分析需求

根据上图，先分析一下我们要使用到的低代码组件。

1. 表单form
2. 表单组formGroup
3. 表单组件：开关switch，输入框input，单选radio，下拉选择框select。
4. 布局组件：标签页布局tab，面板业布局pane。
5. 展示组件：提示alert。

从功能上分析，有以下功能点。

1. 有一个**接入开关**做为总开关。当它关闭时，表单里的所有控件都会变为disable状态。
2. 有一个**反扒拦截**作为二级开关，共同控制详细配置中的控件状态
3. 表单项会有一定的校验
4. 表单项会有一定的其他联动，比如打开开关出现相应的输入框，选择不同的实行方案，会有不同的提示信息。

下面我们就来使用低代码快速的实现这些功能。





## STEP2 拖拽/完善组件

拖拽页面并不是本次示例的重点，所以我门在此带过。先看一下拖拽后的效果和代码结构大纲。

![image-20210820111621149](../../assets/img/example/form/image-20210820111621149.png)

## STEP3 初始化接口字段

根据接口文档填充对应的字段。

```javascript
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "advancedSwitch": 1, 
    "policySwitch": 1,
    "dealScheme": "1",
    "blacklistSwitch": 1,
    "blacklist": "黑名单1,黑名单2",
    "cookieSwitch":1,
    "bodySwitch": 1,
    "bodyLength": 1024,
    "content":"详细内容"
  }
}
```



## STEP4 使用公式`Formula`实现联动

关于`公式`的详细使用见`公式`

### 1.禁用表单

下面我们先来写两个开关，来实现表单的禁用与开启。根据需求分析，我们需要通过接入开关advancedSwitch来判读全部表单的禁用状态，根据反扒拦截开关来判断详细配置中表单项的禁用状态。

先定义`公式`总开关**flag**和和策略开关**policyFlag**如下：

![image-20210819155615853](../../assets/img/example/form/image-20210819155615853.png)

![image-20210819161434023](../../assets/img/example/form/image-20210819161434023.png)

在表单项中使用

![image-20210819161637460](../../assets/img/example/form/image-20210819161637460.png)

![image-20210819162224820](../../assets/img/example/form/image-20210819162224820.png)

其他表单项同理，接下来先看下效果。

```schema
{
	"apis": [],
	"pageConfig": [
		{
			"type": "JPage",
			"options": {
				"title": ""
			},
			"aside": [],
			"body": [
				{
					"type": "JTabsLayout",
					"options": {
						"tabBarGutter": 20
					},
					"columns": [
						{
							"value": "1",
							"label": "安全设置",
							"children": [
								{
									"type": "JForm",
									"options": {
										"layout": "horizontal",
										"labelAlign": "right",
										"labelCol": {
											"span": 0
										},
										"wrapperCol": {
											"span": 12
										},
										"cardOptions": {},
										"labelWidth": 120
									},
									"columns": [
										{
											"type": "JFormItem",
											"name": "advancedSwitch",
											"options": {
												"label": "接入开关",
												"initialValue": 2,
												"itemSuffix": "",
												"labelRemark": "",
												"help": "",
												"colon": false,
												"activeValue": 1,
												"inactiveValue": 2
											},
											"componentType": "JSwitch",
											"labelStyle": "font-size:24px;font-weight:bold;"
										},
										{
											"type": "JDivider",
											"options": {}
										},
										{
											"type": "JFormItem",
											"name": "switch1",
											"options": {
												"label": "黑名单",
												"initialValue": false,
												"itemSuffix": "",
												"labelRemark": "",
												"help": "",
												"colon": false,
												"disabledOn": "flag"
											},
											"componentType": "JSwitch",
											"labelStyle": "font-size:18px;font-weight:bold;"
										},
										{
											"type": "JFormItem",
											"name": "select1",
											"options": {
												"label": "下拉选择框",
												"dynamic": false,
												"allowClear": true,
												"options": [
													{
														"value": "1",
														"label": "选项1"
													},
													{
														"value": "2",
														"label": "选项2"
													}
												],
												"hideLabel": true,
												"multiple": false,
												"useTagMode": true,
												"labelRemark": "请选择",
												"disabledOn": "flag"
											},
											"componentType": "JSelect",
											"style": "width:500px"
										},
										{
											"type": "JFormItem",
											"name": "switch2",
											"options": {
												"label": "接口流量分配",
												"initialValue": false,
												"itemSuffix": "",
												"labelRemark": "",
												"help": "",
												"colon": false,
												"disabledOn": "flag"
											},
											"componentType": "JSwitch",
											"labelStyle": "font-size:18px;font-weight:bold;"
										},
										{
											"type": "JFormGroup",
											"options": {
												"label": "",
												"layout": "inline",
												"labelCol": {},
												"wrapperCol": {},
												"hideLabel": true,
												"groupLabelWidth": null
											},
											"children": [
												{
													"type": "JFormItem",
													"name": "radio1",
													"options": {
														"label": "单选框",
														"dynamic": false,
														"options": [
															{
																"value": 1,
																"label": "全量"
															},
															{
																"value": 2,
																"label": "灰度"
															}
														],
														"hideLabel": true,
														"disabledOn": "flag"
													},
													"componentType": "JRadioGroup"
												},
												{
													"type": "JFormItem",
													"name": "text1",
													"options": {
														"label": "输入框",
														"placeholder": "请输入",
														"hideLabel": true,
														"itemSuffix": "%",
														"initialValue": "",
														"disabledOn": "flag"
													},
													"componentType": "JInput"
												}
											]
										},
										{
											"type": "JSpan",
											"options": {
												"tpl": "传参配置"
											},
											"style": {
												"fontSize": "18px",
												"fontWeight": "bold",
												"color": "#000000"
											}
										},
										{
											"type": "JFormGroup",
											"options": {
												"label": "cookie",
												"layout": "horizontal",
												"labelCol": {},
												"wrapperCol": {},
												"groupLabelWidth": null,
												"labelWidth": null,
												"hideLabel": false
											},
											"children": [
												{
													"type": "JFormItem",
													"name": "switch3",
													"options": {
														"label": "",
														"initialValue": false,
														"disabledOn": "flag"
													},
													"componentType": "JSwitch"
												}
											]
										},
										{
											"type": "JFormGroup",
											"options": {
												"label": "body",
												"layout": "inline",
												"labelCol": {},
												"wrapperCol": {},
												"groupLabelWidth": null,
												"labelWidth": null,
												"hideLabel": false
											},
											"children": [
												{
													"type": "JFormItem",
													"name": "switch4",
													"options": {
														"label": "",
														"initialValue": false,
														"disabledOn": "flag"
													},
													"componentType": "JSwitch"
												},
												{
													"type": "JFormItem",
													"name": "text3",
													"options": {
														"label": "输入框",
														"placeholder": "请输入",
														"hideLabel": true,
														"itemSuffix": "length",
														"disabledOn": "flag"
													},
													"componentType": "JInput"
												}
											]
										},
										{
											"type": "JTabsLayout",
											"options": {
												"tabBarGutter": 20
											},
											"columns": [
												{
													"value": "1",
													"label": "反爬拦截",
													"children": [
														{
															"type": "JFormItem",
															"name": "policySwitch",
															"options": {
																"label": "反扒拦截",
																"initialValue": 2,
																"itemSuffix": "",
																"labelRemark": "",
																"help": "",
																"colon": false,
																"disabledOn": "flag",
																"activeValue": 1,
																"inactiveValue": 2
															},
															"componentType": "JSwitch",
															"labelStyle": "font-size:16px;font-weight:bold;"
														}
													]
												},
												{
													"value": "2",
													"label": "小号拦截",
													"children": []
												}
											]
										},
										{
											"type": "JPanelLayout",
											"headers": [],
											"body": [
												{
													"type": "JForm",
													"options": {
														"layout": "horizontal",
														"labelAlign": "right",
														"labelCol": {
															"span": 0
														},
														"wrapperCol": {
															"span": 12
														},
														"cardOptions": {},
														"labelWidth": null
													},
													"columns": [
														{
															"type": "JFormItem",
															"name": "radio2",
															"options": {
																"label": "实行方案",
																"dynamic": false,
																"options": [
																	{
																		"value": 1,
																		"label": "mock"
																	},
																	{
																		"value": 2,
																		"label": "自定义路由"
																	},
																	{
																		"value": "3",
																		"label": "熔断"
																	}
																],
																"disabledOn": "flag || policyFlag",
																"colon": true,
																"labelWidth": 80
															},
															"componentType": "JRadioGroup"
														},
														{
															"type": "JAlert",
															"options": {
																"type": "success",
																"message": "实际请求不会调用后端接口，由网关直接返回给调用方虚拟数据，返回的内容自己指定，填入“Mock返回结果”",
																"description": "",
																"closable": false,
																"showIcon": true
															},
															"message": [],
															"description": []
														},
														{
															"type": "JFormItem",
															"name": "textarea1",
															"options": {
																"label": "文本框",
																"placeholder": "请输入",
																"hideLabel": true,
																"disabledOn": "flag || policyFlag",
																"labelWidth": 0
															},
															"componentType": "JTextArea",
															"labelStyle": "",
															"style": "margin-top:20px;width:600px"
														}
													]
												}
											],
											"options": {
												"title": "详细配置"
											}
										},
										{
											"type": "JFormGroup",
											"options": {
												"label": "表单组",
												"layout": "inline",
												"labelCol": {},
												"wrapperCol": {},
												"hideLabel": true,
												"labelWidth": null,
												"groupLabelWidth": null
											},
											"children": [
												{
													"type": "JFormItem",
													"options": {
														"text": "提交",
														"level": "primary",
														"label": " ",
														"colon": false
													},
													"componentType": "JAction"
												},
												{
													"type": "JFormItem",
													"options": {
														"text": "重置",
														"level": "default",
														"label": " ",
														"colon": false
													},
													"componentType": "JAction"
												}
											]
										}
									]
								}
							]
						}
					]
				}
			],
			"toolbar": []
		}
	],
	"formulas": [
		{
			"name": "flag",
			"content": "return data.advancedSwitch != 1\n"
		},
		{
			"name": "policyFlag",
			"content": "return data.policySwitch != 1\n"
		}
	]
}
```

### 2.表单联动

这里我们以详细配置中的表单项为例，实现选择不同的实行方案，来展示不同的文案提示和表单项。

先创建一个名为**提示**的公式，在`alert提示`控件中使用。

##### 配置提示的文案

![image-20210819163825424](../../assets/img/example/form/image-20210819163825424.png)

在`Alert提示`中使用公式

![image-20210820112032611](../../assets/img/example/form/image-20210820112032611.png)

##### 配置表单项的显隐条件

![image-20210820112259256](../../assets/img/example/form/image-20210820112259256.png)

看下效果

```schema
{
	"apis": [
		{
			"name": "get",
			"config": {
				"url": "http://11.104.65.148/formExample",
				"cache": 0,
				"silent": false,
				"data": {},
				"dataType": "json",
				"method": "get",
				"replaceData": false,
				"sendOn": "",
				"reloadIgnoreKeys": null,
				"headers": {},
				"requestAdaptor": "",
				"adaptor": "",
				"responseType": ""
			}
		},
		{
			"name": "提交",
			"config": {
				"url": "http://11.104.65.148/formExample",
				"cache": 0,
				"silent": false,
				"data": {
					"advancedSwitch": "${advancedSwitch}",
					"policySwitch": "${policySwitch}",
					"dealScheme": "${dealScheme}",
					"blacklistSwitch": "${blacklistSwitch}",
					"blacklist": "${blacklist}",
					"cookieSwitch": "${cookieSwitch}",
					"bodySwitch": "${bodySwitch}",
					"bodyLength": "${bodyLength}",
					"content": "${content}"
				},
				"dataType": "json",
				"method": "put",
				"replaceData": false,
				"sendOn": "",
				"reloadIgnoreKeys": null,
				"headers": {},
				"requestAdaptor": "",
				"adaptor": "",
				"responseType": ""
			}
		}
	],
	"pageConfig": [
		{
			"type": "JPage",
			"options": {
				"title": ""
			},
			"aside": [],
			"body": [
				{
					"type": "JTabsLayout",
					"options": {
						"tabBarGutter": 20
					},
					"columns": [
						{
							"value": "1",
							"label": "安全设置",
							"children": [
								{
									"type": "JForm",
									"options": {
										"layout": "horizontal",
										"labelAlign": "right",
										"labelCol": {
											"span": 0
										},
										"wrapperCol": {
											"span": 12
										},
										"cardOptions": {},
										"labelWidth": 120,
										"initApi": null,
										"api": null
									},
									"columns": [
										{
											"type": "JFormItem",
											"name": "advancedSwitch",
											"options": {
												"label": "接入开关",
												"initialValue": 2,
												"itemSuffix": "",
												"labelRemark": "",
												"help": "",
												"colon": false,
												"activeValue": 1,
												"inactiveValue": 2
											},
											"componentType": "JSwitch",
											"labelStyle": "font-size:24px;font-weight:bold;"
										},
										{
											"type": "JDivider",
											"options": {}
										},
										{
											"type": "JFormItem",
											"name": "blacklistSwitch",
											"options": {
												"label": "黑名单",
												"initialValue": false,
												"itemSuffix": "",
												"labelRemark": "",
												"help": "",
												"colon": false,
												"disabledOn": "flag"
											},
											"componentType": "JSwitch",
											"labelStyle": "font-size:18px;font-weight:bold;"
										},
										{
											"type": "JFormItem",
											"name": "blacklist",
											"options": {
												"label": "下拉选择框",
												"dynamic": false,
												"allowClear": true,
												"options": [
													{
														"value": "1",
														"label": "选项1"
													},
													{
														"value": "2",
														"label": "选项2"
													}
												],
												"hideLabel": true,
												"multiple": false,
												"useTagMode": true,
												"labelRemark": "请选择",
												"disabledOn": "flag"
											},
											"componentType": "JSelect",
											"style": "width:500px"
										},
										{
											"type": "JSpan",
											"options": {
												"tpl": "传参配置"
											},
											"style": {
												"fontSize": "18px",
												"fontWeight": "bold",
												"color": "#000000"
											}
										},
										{
											"type": "JFormGroup",
											"options": {
												"label": "cookie",
												"layout": "horizontal",
												"labelCol": {},
												"wrapperCol": {},
												"groupLabelWidth": null,
												"labelWidth": null,
												"hideLabel": false
											},
											"children": [
												{
													"type": "JFormItem",
													"name": "cookieSwitch",
													"options": {
														"label": "",
														"initialValue": false,
														"disabledOn": "flag"
													},
													"componentType": "JSwitch"
												}
											]
										},
										{
											"type": "JFormGroup",
											"options": {
												"label": "body",
												"layout": "inline",
												"labelCol": {},
												"wrapperCol": {},
												"groupLabelWidth": null,
												"labelWidth": null,
												"hideLabel": false
											},
											"children": [
												{
													"type": "JFormItem",
													"name": "bodySwitch",
													"options": {
														"label": "",
														"initialValue": false,
														"disabledOn": "flag"
													},
													"componentType": "JSwitch"
												},
												{
													"type": "JFormItem",
													"name": "bodyLength",
													"options": {
														"label": "输入框",
														"placeholder": "请输入",
														"hideLabel": true,
														"itemSuffix": "length",
														"disabledOn": "flag"
													},
													"componentType": "JInput"
												}
											]
										},
										{
											"type": "JTabsLayout",
											"options": {
												"tabBarGutter": 20
											},
											"columns": [
												{
													"value": "1",
													"label": "反爬拦截",
													"children": [
														{
															"type": "JFormItem",
															"name": "policySwitch",
															"options": {
																"label": "反扒拦截",
																"initialValue": 2,
																"itemSuffix": "",
																"labelRemark": "",
																"help": "",
																"colon": false,
																"disabledOn": "flag",
																"activeValue": 1,
																"inactiveValue": 2
															},
															"componentType": "JSwitch",
															"labelStyle": "font-size:16px;font-weight:bold;"
														}
													]
												},
												{
													"value": "2",
													"label": "小号拦截",
													"children": []
												}
											]
										},
										{
											"type": "JPanelLayout",
											"headers": [],
											"body": [
												{
													"type": "JForm",
													"options": {
														"layout": "horizontal",
														"labelAlign": "right",
														"labelCol": {
															"span": 0
														},
														"wrapperCol": {
															"span": 12
														},
														"cardOptions": {},
														"labelWidth": null
													},
													"columns": [
														{
															"type": "JFormItem",
															"name": "dealScheme",
															"options": {
																"label": "实行方案",
																"dynamic": false,
																"options": [
																	{
																		"value": 1,
																		"label": "mock"
																	},
																	{
																		"value": 2,
																		"label": "自定义路由"
																	},
																	{
																		"value": "3",
																		"label": "熔断"
																	}
																],
																"disabledOn": "flag || policyFlag",
																"colon": true,
																"labelWidth": 80
															},
															"componentType": "JRadioGroup"
														},
														{
															"type": "JAlert",
															"options": {
																"type": "success",
																"message": "${提示}",
																"description": "",
																"closable": false,
																"showIcon": true
															},
															"message": [],
															"description": []
														},
														{
															"type": "JFormItem",
															"name": "content",
															"options": {
																"label": "下拉选择框",
																"dynamic": false,
																"allowClear": true,
																"options": [
																	{
																		"value": "1",
																		"label": "403"
																	}
																],
																"hiddenOn": "data.dealScheme != 2"
															},
															"componentType": "JSelect",
															"style": "margin-top:20px;width:600px"
														},
														{
															"type": "JFormItem",
															"name": "content",
															"options": {
																"label": "文本框",
																"placeholder": "请输入",
																"hideLabel": true,
																"disabledOn": "flag || policyFlag",
																"labelWidth": 0,
																"hiddenOn": "data.dealScheme == 1 || data.dealScheme ==2"
															},
															"componentType": "JTextArea",
															"labelStyle": "",
															"style": "margin-top:20px;width:600px"
														}
													]
												}
											],
											"options": {
												"title": "详细配置"
											}
										},
										{
											"type": "JFormGroup",
											"options": {
												"label": "表单组",
												"layout": "inline",
												"labelCol": {},
												"wrapperCol": {},
												"hideLabel": true,
												"labelWidth": null,
												"groupLabelWidth": null
											},
											"children": [
												{
													"type": "JFormItem",
													"options": {
														"text": "提交",
														"level": "primary",
														"label": " ",
														"colon": false,
														"type": "submit"
													},
													"componentType": "JAction"
												},
												{
													"type": "JFormItem",
													"options": {
														"text": "重置",
														"level": "default",
														"label": " ",
														"colon": false,
														"type": "reset"
													},
													"componentType": "JAction"
												}
											]
										}
									]
								}
							]
						}
					]
				}
			],
			"toolbar": []
		}
	],
	"formulas": [
		{
			"name": "flag",
			"content": "return data.advancedSwitch != 1\n"
		},
		{
			"name": "policyFlag",
			"content": "return data.policySwitch != 1\n"
		},
		{
			"name": "提示",
			"content": "var dealScheme = data.dealScheme;\ndealScheme = dealScheme ? Number(dealScheme) : 0;\nswitch(dealScheme){\n  case 1:\n   return \"实际请求不会调用后端接口，由网关直接返回给调用方虚拟数据，返回的内容自己指定，填入“Mock返回结果”\";\n  case 2:\n    return \"自定义需要路由的服务地址\";\n  case 3:\n    return \"熔断后则直接拦截流量，网关直接返回403\"\n  default:\n    return \"实际请求不会调用后端接口，由网关直接返回给调用方虚拟数据，返回的内容自己指定，填入“Mock返回结果”\"\n}"
		}
	]
}
```

## STEP5 表单提交与重置

#### 配置Api

关于Api的使用详见`Api`



![image-20210819181807025](../../assets/img/example/form/image-20210819181807025.png)

#### 配置form

![image-20210819184607411](../../assets/img/example/form/image-20210819184607411.png)

#### 配置提交与重置按钮

按钮置于表单中，操作类型选择为提交与重置。

（需要注意，重置表单如果不在后执行中刷新表单，会清除所有表单数据）

![image-20210819183318528](../../assets/img/example/form/image-20210819183318528.png)





## 完成

看下效果



```schema
{
	"apis": [
		{
			"name": "get",
			"config": {
				"url": "http://11.104.65.148/formExample",
				"cache": 0,
				"silent": false,
				"data": {},
				"dataType": "json",
				"method": "get",
				"replaceData": false,
				"sendOn": "",
				"reloadIgnoreKeys": null,
				"headers": {},
				"requestAdaptor": "",
				"adaptor": "",
				"responseType": ""
			}
		},
		{
			"name": "提交",
			"config": {
				"url": "http://11.104.65.148/formExample",
				"cache": 0,
				"silent": false,
				"data": {
					"advancedSwitch": "${advancedSwitch}",
					"policySwitch": "${policySwitch}",
					"dealScheme": "${dealScheme}",
					"blacklistSwitch": "${blacklistSwitch}",
					"blacklist": "${blacklist}",
					"cookieSwitch": "${cookieSwitch}",
					"bodySwitch": "${bodySwitch}",
					"bodyLength": "${bodyLength}",
					"content": "${content}"
				},
				"dataType": "json",
				"method": "put",
				"replaceData": false,
				"sendOn": "",
				"reloadIgnoreKeys": null,
				"headers": {},
				"requestAdaptor": "",
				"adaptor": "",
				"responseType": ""
			}
		}
	],
	"pageConfig": [
		{
			"type": "JPage",
			"options": {
				"title": ""
			},
			"aside": [],
			"body": [
				{
					"type": "JTabsLayout",
					"options": {
						"tabBarGutter": 20
					},
					"columns": [
						{
							"value": "1",
							"label": "安全设置",
							"children": [
								{
									"type": "JForm",
									"options": {
										"layout": "horizontal",
										"labelAlign": "right",
										"labelCol": {
											"span": 0
										},
										"wrapperCol": {
											"span": 12
										},
										"cardOptions": {},
										"labelWidth": 120,
										"initApi": "$$get",
										"api": "$$提交"
									},
									"columns": [
										{
											"type": "JFormItem",
											"name": "advancedSwitch",
											"options": {
												"label": "接入开关",
												"initialValue": 2,
												"itemSuffix": "",
												"labelRemark": "",
												"help": "",
												"colon": false,
												"activeValue": 1,
												"inactiveValue": 2
											},
											"componentType": "JSwitch",
											"labelStyle": "font-size:24px;font-weight:bold;"
										},
										{
											"type": "JDivider",
											"options": {}
										},
										{
											"type": "JFormItem",
											"name": "blacklistSwitch",
											"options": {
												"label": "黑名单",
												"initialValue": false,
												"itemSuffix": "",
												"labelRemark": "",
												"help": "",
												"colon": false,
												"disabledOn": "flag"
											},
											"componentType": "JSwitch",
											"labelStyle": "font-size:18px;font-weight:bold;"
										},
										{
											"type": "JFormItem",
											"name": "blacklist",
											"options": {
												"label": "下拉选择框",
												"dynamic": false,
												"allowClear": true,
												"options": [
													{
														"value": "1",
														"label": "选项1"
													},
													{
														"value": "2",
														"label": "选项2"
													}
												],
												"hideLabel": true,
												"multiple": false,
												"useTagMode": true,
												"labelRemark": "请选择",
												"disabledOn": "flag"
											},
											"componentType": "JSelect",
											"style": "width:500px"
										},
										{
											"type": "JSpan",
											"options": {
												"tpl": "传参配置"
											},
											"style": {
												"fontSize": "18px",
												"fontWeight": "bold",
												"color": "#000000"
											}
										},
										{
											"type": "JFormGroup",
											"options": {
												"label": "cookie",
												"layout": "horizontal",
												"labelCol": {},
												"wrapperCol": {},
												"groupLabelWidth": null,
												"labelWidth": null,
												"hideLabel": false
											},
											"children": [
												{
													"type": "JFormItem",
													"name": "cookieSwitch",
													"options": {
														"label": "",
														"initialValue": false,
														"disabledOn": "flag"
													},
													"componentType": "JSwitch"
												}
											]
										},
										{
											"type": "JFormGroup",
											"options": {
												"label": "body",
												"layout": "inline",
												"labelCol": {},
												"wrapperCol": {},
												"groupLabelWidth": null,
												"labelWidth": null,
												"hideLabel": false
											},
											"children": [
												{
													"type": "JFormItem",
													"name": "bodySwitch",
													"options": {
														"label": "",
														"initialValue": false,
														"disabledOn": "flag"
													},
													"componentType": "JSwitch"
												},
												{
													"type": "JFormItem",
													"name": "bodyLength",
													"options": {
														"label": "输入框",
														"placeholder": "请输入",
														"hideLabel": true,
														"itemSuffix": "length",
														"disabledOn": "flag"
													},
													"componentType": "JInput"
												}
											]
										},
										{
											"type": "JTabsLayout",
											"options": {
												"tabBarGutter": 20
											},
											"columns": [
												{
													"value": "1",
													"label": "反爬拦截",
													"children": [
														{
															"type": "JFormItem",
															"name": "policySwitch",
															"options": {
																"label": "反扒拦截",
																"initialValue": 2,
																"itemSuffix": "",
																"labelRemark": "",
																"help": "",
																"colon": false,
																"disabledOn": "flag",
																"activeValue": 1,
																"inactiveValue": 2
															},
															"componentType": "JSwitch",
															"labelStyle": "font-size:16px;font-weight:bold;"
														}
													]
												},
												{
													"value": "2",
													"label": "小号拦截",
													"children": []
												}
											]
										},
										{
											"type": "JPanelLayout",
											"headers": [],
											"body": [
												{
													"type": "JForm",
													"options": {
														"layout": "horizontal",
														"labelAlign": "right",
														"labelCol": {
															"span": 0
														},
														"wrapperCol": {
															"span": 12
														},
														"cardOptions": {},
														"labelWidth": null
													},
													"columns": [
														{
															"type": "JFormItem",
															"name": "dealScheme",
															"options": {
																"label": "实行方案",
																"dynamic": false,
																"options": [
																	{
																		"value": 1,
																		"label": "mock"
																	},
																	{
																		"value": 2,
																		"label": "自定义路由"
																	},
																	{
																		"value": "3",
																		"label": "熔断"
																	}
																],
																"disabledOn": "flag || policyFlag",
																"colon": true,
																"labelWidth": 80
															},
															"componentType": "JRadioGroup"
														},
														{
															"type": "JAlert",
															"options": {
																"type": "success",
																"message": "${提示}",
																"description": "",
																"closable": false,
																"showIcon": true
															},
															"message": [],
															"description": []
														},
														{
															"type": "JFormItem",
															"name": "content",
															"options": {
																"label": "下拉选择框",
																"dynamic": false,
																"allowClear": true,
																"options": [
																	{
																		"value": "1",
																		"label": "403"
																	}
																],
																"hiddenOn": "data.dealScheme != 2"
															},
															"componentType": "JSelect",
															"style": "margin-top:20px;width:600px"
														},
														{
															"type": "JFormItem",
															"name": "content",
															"options": {
																"label": "文本框",
																"placeholder": "请输入",
																"hideLabel": true,
																"disabledOn": "flag || policyFlag",
																"labelWidth": 0,
																"hiddenOn": "data.dealScheme == 1 || data.dealScheme ==2"
															},
															"componentType": "JTextArea",
															"labelStyle": "",
															"style": "margin-top:20px;width:600px"
														}
													]
												}
											],
											"options": {
												"title": "详细配置"
											}
										},
										{
											"type": "JFormGroup",
											"options": {
												"label": "表单组",
												"layout": "inline",
												"labelCol": {},
												"wrapperCol": {},
												"hideLabel": true,
												"labelWidth": null,
												"groupLabelWidth": null
											},
											"children": [
												{
													"type": "JFormItem",
													"options": {
														"text": "提交",
														"level": "primary",
														"label": " ",
														"colon": false,
														"type": "submit"
													},
													"componentType": "JAction"
												},
												{
													"type": "JFormItem",
													"options": {
														"text": "重置",
														"level": "default",
														"label": " ",
														"colon": false,
														"type": "reset"
													},
													"componentType": "JAction"
												}
											]
										}
									]
								}
							]
						}
					]
				}
			],
			"toolbar": []
		}
	],
	"formulas": [
		{
			"name": "flag",
			"content": "return data.advancedSwitch != 1\n"
		},
		{
			"name": "policyFlag",
			"content": "return data.policySwitch != 1\n"
		},
		{
			"name": "提示",
			"content": "var dealScheme = data.dealScheme;\ndealScheme = dealScheme ? Number(dealScheme) : 0;\nswitch(dealScheme){\n  case 1:\n   return \"实际请求不会调用后端接口，由网关直接返回给调用方虚拟数据，返回的内容自己指定，填入“Mock返回结果”\";\n  case 2:\n    return \"自定义需要路由的服务地址\";\n  case 3:\n    return \"熔断后则直接拦截流量，网关直接返回403\"\n  default:\n    return \"实际请求不会调用后端接口，由网关直接返回给调用方虚拟数据，返回的内容自己指定，填入“Mock返回结果”\"\n}"
		}
	]
}
```

你可以在[低代码示例应用](http://beta-jap.jd.com/manage.html#/dev/app-detail/1410938307064725506?color=%236C71E9&theme=default&icon=&menuId=1426092104644657154)中体验它，并了解完整的配置

































