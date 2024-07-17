# 栅格布局
栅格布局用来实现页面百分比布局，基本原理是一个页面被等分为24份，每个栅格占据一定的份额

示例如下：

下面分别演示了栅格布局在12:12、16:8、以及8:8:8比例下的效果（注意：下面的虚线仅仅为了演示，实际部署时并无虚线）。 

```schema
{
  "type": "JPage",
  "options": {
    "title": "栅格示例"
  },
  "aside": [],
  "body": [
    {
      "type": "JFieldSet",
      "options": {
        "title": "2列等宽12:12",
        "collapsable": true
      },
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
            "cardOptions": {}
          },
          "columns": [
            {
              "type": "JGridLayout",
              "options": {},
              "columns": [
                {
                  "span": 12,
                  "children": [
                    {
                      "type": "JFormItem",
                      "name": "userName",
                      "componentType": "JInput",
                      "options": {
                        "label": "用户名",
                        "placeholder": "请输入"
                      }
                    },
                    {
                      "type": "JFormItem",
                      "name": "gender",
                      "componentType": "JInput",
                      "options": {
                        "label": "性别",
                        "placeholder": "请输入"
                      }
                    }
                  ]
                },
                {
                  "span": 12,
                  "children": [
                    {
                      "type": "JFormItem",
                      "name": "userNick",
                      "componentType": "JInput",
                      "options": {
                        "label": "姓名",
                        "placeholder": "请输入"
                      }
                    },
                    {
                      "type": "JFormItem",
                      "name": "age",
                      "componentType": "JInput",
                      "options": {
                        "label": "年龄",
                        "placeholder": "请输入"
                      }
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "JFieldSet",
      "options": {
        "title": "2列16:8",
        "collapsable": true
      },
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
            "cardOptions": {}
          },
          "columns": [
            {
              "type": "JGridLayout",
              "options": {},
              "columns": [
                {
                  "span": 16,
                  "children": [
                    {
                      "type": "JFormItem",
                      "name": "userName",
                      "componentType": "JInput",
                      "options": {
                        "label": "用户名",
                        "placeholder": "请输入"
                      }
                    },
                    {
                      "type": "JFormItem",
                      "name": "gender",
                      "componentType": "JInput",
                      "options": {
                        "label": "性别",
                        "placeholder": "请输入"
                      }
                    }
                  ]
                },
                {
                  "span": 8,
                  "children": [
                    {
                      "type": "JFormItem",
                      "name": "userNick",
                      "componentType": "JInput",
                      "options": {
                        "label": "姓名",
                        "placeholder": "请输入"
                      }
                    },
                    {
                      "type": "JFormItem",
                      "name": "age",
                      "componentType": "JInput",
                      "options": {
                        "label": "年龄",
                        "placeholder": "请输入"
                      }
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "JFieldSet",
      "options": {
        "title": "3列8:8:8",
        "collapsable": true
      },
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
            "cardOptions": {}
          },
          "columns": [
            {
              "type": "JGridLayout",
              "options": {},
              "columns": [
                {
                  "span": 8,
                  "children": [
                    {
                      "type": "JFormItem",
                      "name": "userName",
                      "componentType": "JInput",
                      "options": {
                        "label": "用户名",
                        "placeholder": "请输入"
                      }
                    },
                    {
                      "type": "JFormItem",
                      "name": "gender",
                      "componentType": "JInput",
                      "options": {
                        "label": "性别",
                        "placeholder": "请输入"
                      }
                    }
                  ]
                },
                {
                  "span": 8,
                  "children": [
                    {
                      "type": "JFormItem",
                      "name": "age",
                      "componentType": "JInput",
                      "options": {
                        "label": "年龄",
                        "placeholder": "请输入"
                      }
                    },
                    {
                      "type": "JFormItem",
                      "name": "age",
                      "componentType": "JInput",
                      "options": {
                        "label": "年龄",
                        "placeholder": "请输入"
                      }
                    }
                  ]
                },
                {
                  "span": 8,
                  "children": [
                    {
                      "type": "JFormItem",
                      "name": "other",
                      "componentType": "JInput",
                      "options": {
                        "label": "其他",
                        "placeholder": "请输入"
                      }
                    },
                    {
                      "type": "JFormItem",
                      "name": "remark",
                      "componentType": "JInput",
                      "options": {
                        "label": "备注",
                        "placeholder": "请输入"
                      }
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
```



# 2列布局

2列布局的话需要将每个栅格设置为12

# 属性表

| 属性名             | 配置名         | 类型              | 默认值      | 说明                         |
| ------------------ | -------------- | ----------------- | ----------- | ---------------------------- |
| type               |                | string            | JGridLayout |                              |
| columns[].span     | 栅格占据的宽度 | number            |             | 每个栅格占据的宽度，1~24之间 |
| columns[].children | 栅格的子项内容 | Array&lt;SchemaNode&gt; |             | 每个栅格里的那内容           |
