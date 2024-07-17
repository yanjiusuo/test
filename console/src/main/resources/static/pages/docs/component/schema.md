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
type：JPage表示这个组件是页面类型，title属性表示这个页面的标题为`测试页`。   
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
      "toolbar": [],
      "key": "1c21b20f27c7410eaa54e2fdabe5a9b6",
      "__isComp": true
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
body内可以嵌套别的组件，渲染引擎通过type字段定位组件