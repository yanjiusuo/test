# 自定义HTML组件

当我们使用APAAS的时候，遇到了一些定制的需求，无法通过拖拉拽来实现时，该怎么办呢？

比如说下面这种情况：

![image-20210823143413820](../../assets/img/example/html/image-20210823143413820.png)

这时候，就可以选择使用`自定义html组件`来实现这种需求。

> 请注意:使用此组件可能需要您有一定的`html/css`基础。

下面是一个关于`自定义Html`组件使用的详细示例。

## STEP1 手写HTML

### 1.解析HTML

我们先根据需求手写html/css，html结构如下

```html
<html>
 <head></head>
 <body>
  <div class="bigbox"> 
   <div class="bigbox2"> 
    <div class="title"> 
     <p>文章标题</p> 
     <div class="spanner"> 
      <span class="spanner1">文章来源</span> 
      <span>文章时间</span> 
     </div> 
    </div> 
    <div class="freebox"> 
     <div class="content"> 
      <div> 
       <span>文章内容</span> 
      </div> 
     </div> 
    </div> 
    <p class="landUrl ">查看详情</p> 
   </div> 
   <div class="footer"> 
    <button type="button" class="button ant-btn ant-btn-default">上一条</button> 
    <button type="button" class="button ant-btn ant-btn-default">下一条</button> 
   </div> 
  </div>
 </body>
</html>
```

将它导入到`自定义html组件`，点击配置模板并解析代码

![image-20210820182509432](../../assets/img/example/html/image-20210820182509432.png)

获得低代码的解析结果之后，根据需求我们勾选按钮部分的可点击状态。

![image-20210820182809914](../../assets/img/example/html/image-20210820182809914.png)

### 2.导入CSS

![image-20210820182920288](../../assets/img/example/html/image-20210820182920288.png)

点击确定，预览页面

![image-20210820183053716](../../assets/img/example/html/image-20210820183053716.png)

第一步完成。

## STEP2 配置接口字段

定义接口内容如下

```json
    {
      "id": 1,
      "title": "消息标题-1",
      "bizSource": "APASS示例",
      "content": "详细内容",
      "createTime": "1628491499910",
      "nextMsgId": 2
    }
```

在页面中初始化接口，并在控件中使用表达式配置字段的值，以下是部分截图。

![image-20210820184349017](../../assets/img/example/html/image-20210820184349017.png)

>APAAS提供了一定的数据映射能力，比如控件2中对时间戳的转化，详细使用见[数据映射](http://wk.jd.com:4000/component/global/data-mapping.html)

## STEP3 配置事件

APAAS在`自定义html组件`中的`事件配置`中提供了与`action行为`一致的动作配置与能力，详细使用见[`action`](http://wk.jd.com:4000/component/components/input/action.html)。

### 使用执行表达式

我们这里要实现的是通过点击<上一条><下一条>来实现消息翻页。也许你会想到使用事件中的调用接口然后在后执行中刷新组件来实现它。但是这样其实是有问题的——在调用接口之后，刷新组件也会调用一次接口并更新页面内容，导致页面内容无变化，所以我们要使用别的方式来实现它，即`执行表达式`。

![image-20210823111655311](../../assets/img/example/html/image-20210823111655311.png)

根据设计器中表达式的提示，已知表达式的执行上下文中存在data、env变量,env中提供了setFieldsValue方法来更新表单值。然后我们就可以着手实现我们需要执行的操作：首先更新form中的msgid，然后调用更新组件刷新form，在form刷新时的接口中使用最新的msgid，就可以实现我们需要的<上一条><下一条>的效果。简单配置如下：

![image-20210823112114175](../../assets/img/example/html/image-20210823112114175.png)

配置完成，看一下效果，翻页已经能正常使用了。



## 完成

再配置一下查看详情的跳转行为与逻辑之后，一个使用自定义Html组件实现的消息查看与跳转的需求就完成了。看下效果：

```schema
{
	"apis": [
		{
			"name": "初始消息查看",
			"config": {
				"url": "http://11.104.65.148/htmlExample?id=${msgid}",
				"cache": 0,
				"data": {},
				"dataType": "json",
				"method": "get",
				"replaceData": false,
				"sendOn": "",
				"headers": {},
				"requestAdaptor": "",
				"adaptor": "payload.status=0;\nreturn payload",
				"responseType": ""
			}
		},
		{
			"name": "标记已读",
			"config": {
				"url": "${baseUrl}/jnos.middleground.msg.updateUnReadedCountV3",
				"cache": 0,
				"data": {
					"body": "{\"languageCode\": \"zh_CN\",\"msgId\":[${msgId|json}],\"appName\": \"jnos-operate\",\"platform\": \"pc\"}",
					"functionId": "updateUnReadedCountV3"
				},
				"dataType": "form",
				"method": "post",
				"replaceData": false,
				"sendOn": "",
				"headers": {
					"Content-Type": "application/x-www-form-urlencoded"
				},
				"requestAdaptor": "",
				"adaptor": "payload.status=0;\nreturn payload",
				"responseType": ""
			}
		}
	],
	"pageConfig": [
		{
			"type": "JPage",
			"options": {
				"title": "",
				"initApi": null,
				"tips": "",
				"id": "page"
			},
			"aside": [],
			"body": [
				{
					"type": "JForm",
					"options": {
						"layout": "horizontal",
						"labelAlign": "right",
						"labelCol": {
							"span": 5
						},
						"wrapperCol": {
							"span": 12
						},
						"cardOptions": {},
						"initApi": "$$初始消息查看"
					},
					"columns": [
						{
							"type": "JHtmlSlot",
							"childAttrs": {
								"p0": {
									"label": "控件0",
									"name": "p0",
									"defaultVal": "文章标题",
									"supportClick": false,
									"action": {},
									"value": "${title}"
								},
								"span1": {
									"label": "控件1",
									"name": "span1",
									"defaultVal": "文章来源",
									"supportClick": false,
									"value": "来源:${bizSource}"
								},
								"span2": {
									"label": "控件2",
									"name": "span2",
									"defaultVal": "文章时间",
									"supportClick": false,
									"value": "时间：${createTime|date:YYYY-MM-DD HH\\:mm\\:ss:x}"
								},
								"span3": {
									"label": "控件3",
									"name": "span3",
									"defaultVal": "文章内容",
									"supportClick": false,
									"value": "${content}"
								},
								"p4": {
									"label": "控件4",
									"name": "p4",
									"defaultVal": "查看详情",
									"supportClick": true,
									"action": {},
									"hiddenOn": "!data.jumpUrl"
								},
								"button5": {
									"label": "控件5",
									"name": "button5",
									"defaultVal": "上一条",
									"supportClick": true,
									"action": {
										"type": "expr",
										"api": "$$上一条",
										"reload": "page",
										"expr": "env.setFieldsValue({id:data.lastMsgId})"
									},
									"hiddenOn": "!data.lastMsgId"
								},
								"button6": {
									"label": "控件6",
									"name": "button6",
									"defaultVal": "下一条",
									"supportClick": true,
									"action": {
										"type": "expr",
										"api": "$$下一条",
										"reload": "page",
										"expr": "env.setFieldsValue({id:data.nextMsgId})"
									},
									"hiddenOn": "!data.nextMsgId"
								}
							},
							"options": {
								"tpl": "\n \n \n  <div class=\"bigbox\"> \n   <div class=\"bigbox2\"> \n    <div class=\"title\"> \n     <p>@p0:控件0:文章标题@</p> \n     <div class=\"spanner\"> \n      <span class=\"spanner1\">@span1:控件1:文章来源@</span> \n      <span>@span2:控件2:文章时间@</span> \n     </div> \n    </div> \n    <div class=\"freebox\"> \n     <div class=\"content\"> \n      <div> \n       <span>@span3:控件3:文章内容@</span> \n      </div> \n     </div> \n    </div> \n    <p class=\"landUrl \" @supportClick=\"@p4:控件4@\">@p4:控件4:查看详情@</p> \n   </div> \n   <div class=\"footer\"> \n    <button type=\"button\" class=\"button ant-btn ant-btn-default\" @supportClick=\"@button5:控件5@\">@button5:控件5:上一条@</button> \n    <button type=\"button\" class=\"button ant-btn ant-btn-default\" @supportClick=\"@button6:控件6@\">@button6:控件6:下一条@</button> \n   </div> \n  </div>\n \n"
							},
							"style": "* {\n\tmargin: 0px;\n\tpadding: 0px;\n}\n\n.bigbox .freebox1 .btn {\n\tbackground-color: transparent;\n\tborder: 1px solid #fff;\n\toutline: none;\n}\n\n.bigbox .freebox1 p {\n\tmargin: 0 10px 3px;\n}\n\n.bigbox .bigbox2 {\n\tmargin-top: 20px;\n\toverflow: hidden;\n}\n\n.bigbox .title {\n\tdisplay: flex;\n\tflex-direction: column;\n\talign-items: center;\n\tmargin-bottom: 20px;\n\tborder-bottom: #E1E1E8 solid 1px;\n\tpadding-bottom: 20px;\n}\n\n.bigbox .title p {\n\tfont-size: 20px;\n\tfont-family: 'Franklin Gothic Medium', 'Arial Narrow', Arial, sans-serif;\n\tcolor: rgba(42,43,46,1);\n\tfont-weight: 600;\n}\n\n.bigbox .spanner {\n\tmargin-top: -10px;\n\tpadding: 0 10px;\n\tcolor: rgba(154,154,159,1);\n}\n\n.bigbox .spanner .spanner1 {\n\tmargin-right: 20px;\n}\n\n.bigbox .freebox {\n}\n\n.bigbox .content {\n\tborder: none;\n\twidth: 1000px;\n\theight: 100%;\n\tfont-size: 14px;\n\tcolor: rgba(42,43,46,1);\n}\n\n.bigbox .content span {\n\tfont-size: 14px;\n\tline-height: 23px;\n}\n\n.bigbox .content a:link {\n\ttext-decoration: none;\n}\n\n.bigbox .content a:link {\n\tcolor: purple;\n}\n\n.bigbox .content a:active {\n\tcolor: red;\n}\n\n.bigbox .content a:visited {\n\tcolor: yellow;\n}\n\n.bigbox .content a:hover {\n\tcolor: green;\n}\n\n.bigbox {\n\tmargin: 0 auto;\n\twidth: 90%;\n\tposition: relative;\n}\n\n.bigbox .footer {\n\tborder-top: #E1E1E8 solid 1px;\n\tmargin-top: 300px;\n\twidth: 100%;\n\theight: 60px;\n\tjustify-content: flex-end;\n\tdisplay: flex;\n\talign-items: center;\n}\n\n.bigbox .landUrl {\n  width: 89px;\nheight: 14px;\nfont-size: 14px;\nfont-family: PingFangSC;\nfont-weight: normal;\ncolor: rgba(9,92,247,1);\n}"
						}
					]
				}
			],
			"toolbar": []
		}
	],
	"formulas": [
		{
			"name": "createTimeFilter",
			"content": "let time = data.createTime\nreturn time && parseInt(time/1000)"
		},
		{
			"name": "msgid",
			"content": "return data.id || 1"
		}
	]
}
```

你可以在低代码演示应用中

## 更多

关于`执行表达更多`的使用方法

以上面的消息详情为例，设想现在有这么一种情景：

我们通过低代码开发的消息详情页面嵌入到了一个有消息通知的后台框架之中。当我们阅读完一条消息后，需要调用主框架的方法来及时的更新状态。

即：**低代码生成的页面如何与嵌入的主框架进行交互**？现有可行方法是，在主框架中将方法注入到window对象，然后在低代码中使用`执行表达式`来执行。

#### 主框架中的配置

```javascript
 const updateMsgCount ()=>{console.log("消息更新")}
 window.updateMsgCount = updateMsgCount
```

#### 低代码中的配置

![image-20210823120522784](../../assets/img/example/html/image-20210823120522784.png)

这样就可以在低代码中使用主框架的事件了
