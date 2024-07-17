# 页面渲染器
## 什么是页面渲染器
页面渲染器是一个低代码前端页面渲染框架，使用JSON来渲染页面，可以通过设计器在线生成json,极大提高开发效率

## 为什么要做页面渲染器
前端经历了十几年的发展后，技术从原生js->jquery->双向绑定vue、react等技术，开发者需要掌握的技术越来越多，开发一个网页需要掌握的技术越来越多。   
但是针对一个页面来说，它是与技术无关的，一个页面不论是用vue、react或jquery开发，其最终的效果是一致的，因此需要一种方案屏蔽不同技术的实现差异，并随时能够用最新的技术去解释，这就是页面设计器的设计思路。   
 页面设计器采用json去渲染页面，由于json是技术无关的，可以随时用不同的技术去解释json,将json渲染为具体的组件。如：
 ```schema
 {
  "type": "JPage",
  "options": {
    "title": "演示页面"
  },
  "aside": [],
  "body": [
    {
      "type": "JForm",
      "options": {
        "layout": "inline",
        "labelAlign": "right",
        "labelCol": {
          "span": 5
        },
        "wrapperCol": {
          "span": 12
        },
        "target": "crud",
        "cardOptions": {}
      },
      "columns": [
        {
          "type": "JFormItem",
          "name": "userName",
          "componentType": "JInput",
          "options": {
            "label": "用户名",
            "is_required": true,
            "allowClear": true,
            "labelCol": {},
            "wrapperCol": {}
          }
        },
        {
          "type": "JFormItem",
          "name": "je",
          "componentType": "JInputNumber",
          "options": {
            "label": "金额",
            "modelType": 4,
            "allowClear": true
          },
          "meta": {}
        },
        {
          "type": "JFormItem",
          "name": "college",
          "componentType": "JSelect",
          "options": {
            "modelType": 10,
            "is_required": true,
            "label": "学历",
            "options": [
              {
                "value": 1,
                "label": "本科"
              },
              {
                "value": 2,
                "label": "专科"
              }
            ],
            "multiple": false,
            "joinValues": true,
            "delimeter": ",",
            "allowClear": true
          },
          "meta": {}
        },
        {
          "type": "JFormItem",
          "componentType": "JAction",
          "options": {
            "text": "查询",
            "level": "primary",
            "label": " ",
            "colon": false,
            "type": "submit"
          }
        },
        {
          "type": "JFormItem",
          "options": {
            "text": "重置",
            "level": "default",
            "colon": false,
            "label": " ",
            "type": "reset"
          },
          "componentType": "JAction"
        }
      ]
    }
  ],
  "toolbar": []
}
```
这个json描述了一个表单页面,json里的type表示组件类型，上面这个组件有`JPage`、`JForm`、`JInput`、`JSelect`、`JAction`五种组件。针对`JPage`组件，`options.title`属性表示页面标题，body表示页面内容，当前page的页面内容为表单。   



## 使用json写页面的好处
1. 不需要懂前端技术，通过在线设计器直接将json设计出来
2. 页面的每个组件都是json,当开发好一个组件后，完全可以将json复制到另一个地方，这样页面就具有了复用价值。当积累的组件足够多时，需要开发的页面即组件也就越来越少
3. 别人开发好的json可以直接拿过来就用，屏蔽了不同的技术栈

# 组件
页面渲染器的最基础元素就是组件，由一个个组件拼接成页面。每个组件都能用json描述出来，如：
```json
 {
    "type": "JPage",
    "options": {
      "title": "测试页"
    }       
  }
```
type：JPage表示这个组件是页面类型，options为页面的配置信息，`options.title`属性表示这个页面的标题。   
这个页面的渲染效果为：
```schema
{
  "pageConfig": [
    {
      "type": "JPage",
      "options": {
        "title": "页面标题"
      },
      "aside": [],
      "body": [         
      ],
      "toolbar": []
    }
  ],
 
  "formulas": []
}
```

# 组件嵌套
组件支持嵌套，如针对页面组件，可以支持body段
```json
 {
    "type": "JPage",
    "options": {
      "title": "测试页"
    },
    "body": [
      ]       
  }
```
body内可以嵌套别的组件，渲染引擎通过type字段定位组件。如body内嵌套文本、表单：
```schema
{
  "type": "JPage",
  "options": {
    "title": "页面标题"
  },
  "aside": [],
  "body": [
    {
      "type": "JTpl",
      "options": {
        "type": "html",
        "text": "<p>这是一段文本23132112</p>"
      }
    },
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
        "cardOptions": {}
      },
      "columns": [
        {
          "type": "JFormItem",
          "name": "text",
          "componentType": "JInput",
          "options": {
            "label": "输入框",
            "placeholder": "请输入"
          }
        },
        {
          "type": "JFormItem",
          "componentType": "JAction",
          "options": {
            "text": "提交",
            "level": "primary",
            "label": " ",
            "colon": false,
            "type": "submit"
          }
        }
      ]
    }
  ],
  "toolbar": []
}
```
JTpl为模板文本，body内嵌套了一个模板文本组件。
组件与组件之间构成组件树,如上面页面就构成一个组件树：   
```
JPage
|------JTpl 
|------JForm
|         |-----JInput
|         |-----JAction
```   
树的最顶级节点是`JPage`,`JPage`下内置JTpl、JForm两个组件，然后JForm里又嵌套JInput及Action两个组件。
