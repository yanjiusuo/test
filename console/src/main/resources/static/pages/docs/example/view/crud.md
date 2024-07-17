# 一个应用管理是如何拖拽出来的？

下面是关于增删改查使用的保姆级教程



先看下最终成果，然后开工。

![image-20210813160727040](../../assets/img/example/crud/image-20210813160727040.png)


## STEP1 分析需求

根据上图，先分析一下我们要用到的组件：

1. 增删改查crud
2. 表单form
3. 按钮Button



## STEP2 拖拽组件

不多说，直接上图。

![image-20210813161514442](../../assets/img/example/crud/image-20210813161514442.png)



## STEP3 完善组件

很明显，组件拖拽过来之后和最终成品还是有很大差距。下一步我们来拆解下如何让页面向成品靠拢。

1. 将表单样式设置为行内，复制一个输入框，复制一个按钮。将输入框的lable改为应用ID和应用名称。将按钮的文案设置成提交和重置(可以顺便把重置按钮的风格设置为default)。
2. 将crud中的按钮文案改为增加应用(可以顺便给它加一个icon)
3. 配置crud中的字段。并配置操作栏。![image-20210813163143043](../../assets/img/example/crud/image-20210813163143043.png)

看一下初步的成果：

![image-20210813163328496](../../assets/img/example/crud/image-20210813163328496.png)

不能说一模一样，只能说完全一致。当然这只是看得到的部分，还有看不到的部分---弹窗。

下面通过按钮Action来配置弹窗，在设计器中点击 添加应用按钮->操作类型->选择打开对话框->配置对话框（关于action的更多使用，详见`action`）

![image-20210813164222445](../../assets/img/example/crud/image-20210813164222445.png)

然后删除用不到的按钮，复制出三个输入框和一个下拉选择框，根据原型修改他们的label（顺便打开他们’必填‘的选项开关）。

直接看结果

![image-20210813164904535](../../assets/img/example/crud/image-20210813164904535.png)

注意右下角的`导入别的配置`这个按钮，点击crud操作栏中的修改，跟上面一样选择配置对话框。然后使用这个`导入别的配置`,找到上面弹窗里配置的内容`表单`

![image-20210813165428642](../../assets/img/example/crud/image-20210813165428642.png)

点击确定，收工。

到这里纯前端的部分就完成了，下面我们来联调接口



## STEP4 联调接口

先分析一下需求，这个需求中有最基础的**增 删 改 查**四个接口

- 增：新增应用按钮
- 删：列表的删除按钮
- 改：列表的编辑按钮
- 查：列表的查询接口和表单的条件查询

先配置接口如下(关于api的使用，详见`api`)

<img src="../../assets/img/example/crud/image-20210813171535727.png" alt="image-20210813171535727" style="zoom:100%;" />

下面开始联调

### 查

- 在设计器中点击列表，打开`是否初始化数据`，配置`初始数据api`，选择配置好的查询接口，并将列表的的id属性配置为crud。
- 在设计器中点击表单，选择数据提交api，选择配置好的查询接口，并将form的target属性配置为上面的crud。

### 增

点开添加应用按钮的`配置对话框`，在弹窗的设计器中点击表单，展开数据提交api，选择配置好的新增接口。

### 删

点击列表中的删除按钮，操作类型选择为`发送请求`，选择配置好的删除接口。

### 改

点击列表中的修改按钮，操作类型选择为`发送请求`，选择配置好的编辑接口。

别忘了要配置好所有的数据字段(在此就不演示了)。

这样，一个具有增删改查功能的应用列表管理的需求就完成了，熟练的话，一个人操作五分钟左右就可以完成上面的工作。



## 看下效果

```schema
{
  "type": "JPage",
  "options": {
    "title": "页面标题"
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
        "cardOptions": {
          "title": "搜索框"
        },
        "wrapWithPane": false,
        "target": "crud"
      },
      "columns": [
        {
          "type": "JFormItem",
          "name": "app_id",
          "componentType": "JInput",
          "options": {
            "label": "应用id",
            "placeholder": "请输入",
            "labelCol": {},
            "wrapperCol": {}
          }
        },
        {
          "type": "JFormItem",
          "name": "app_name",
          "componentType": "JInput",
          "options": {
            "label": "应用名称",
            "placeholder": "请输入",
            "labelCol": {},
            "wrapperCol": {}
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
        },
        {
          "type": "JFormItem",
          "componentType": "JAction",
          "options": {
            "text": "重置",
            "level": "default",
            "label": " ",
            "colon": false,
            "type": "reset"
          }
        }
      ]
    },
    {
      "type": "JCrud",
      "columns": [
        {
          "type": "JText",
          "name": "id",
          "options": {
            "label": "应用id",
            "options": [],
            "sortable": true
          }
        },
        {
          "type": "JText",
          "name": "app_name",
          "options": {
            "label": "应用名称",
            "sortable": true
          }
        },
        {
          "type": "JText",
          "options": {
            "label": "创建人",
            "sortable": true
          },
          "name": "creator"
        },
        {
          "type": "JText",
          "options": {
            "label": "应用描述"
          },
          "name": "app_desc"
        },
        {
          "type": "JText",
          "options": {
            "label": "应用部门"
          },
          "name": "app_dept"
        },
        {
          "type": "JText",
          "options": {
            "label": "应用状态",
            "dynamic": false,
            "options": [
              {
                "value": 1,
                "label": "启用",
                "color": "#7ED321"
              },
              {
                "value": 2,
                "label": "禁用",
                "color": "#E92525"
              }
            ],
            "sortable": true
          },
          "name": "app_status"
        },
        {
          "type": "JButtonGroup",
          "options": {
            "label": "操作"
          },
          "buttons": [
            {
              "type": "JAction",
              "options": {
                "text": "修改",
                "level": "link",
                "type": "dialog",
                "dialog": {
                  "type": "JDialog",
                  "options": {
                    "title": "标题",
                    "okText": "确定",
                    "cancelText": "关闭"
                  },
                  "header": [],
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
                        "api": {
                          "url": "http://jap-mock-data.jd.local/appList/${id}",
                          "cache": 0,
                          "silent": false,
                          "data": {
                            "&": "$$"
                          },
                          "dataType": "json",
                          "method": "put",
                          "replaceData": false,
                          "sendOn": "",
                          "headers": {},
                          "requestAdaptor": "",
                          "adaptor": "",
                          "responseType": "",
                          "blob": ""
                        },
                        "initApi": null
                      },
                      "columns": [
                        {
                          "type": "JFormItem",
                          "name": "app_name",
                          "componentType": "JInput",
                          "options": {
                            "label": "应用名称",
                            "placeholder": "请输入",
                            "labelCol": {},
                            "wrapperCol": {},
                            "required": true
                          }
                        },
                        {
                          "type": "JFormItem",
                          "name": "app_dept",
                          "componentType": "JInput",
                          "options": {
                            "label": "应用部门",
                            "placeholder": "请输入",
                            "labelCol": {},
                            "wrapperCol": {},
                            "required": true
                          }
                        },
                        {
                          "type": "JFormItem",
                          "name": "app_desc",
                          "componentType": "JInput",
                          "options": {
                            "label": "应用描述",
                            "placeholder": "请输入",
                            "labelCol": {},
                            "wrapperCol": {},
                            "required": true
                          }
                        },
                        {
                          "type": "JFormItem",
                          "name": "app_status",
                          "componentType": "JSelect",
                          "options": {
                            "label": "应用状态",
                            "dynamic": false,
                            "allowClear": true,
                            "options": [
                              {
                                "value": 1,
                                "label": "启用"
                              },
                              {
                                "value": 2,
                                "label": "禁用"
                              }
                            ],
                            "labelCol": {},
                            "wrapperCol": {},
                            "required": true
                          }
                        }
                      ]
                    }
                  ],
                  "footer": [
                    {
                      "type": "JAction",
                      "options": {
                        "text": "确定",
                        "level": "primary",
                        "type": "submit",
                        "close": true,
                        "reload": "crud"
                      }
                    },
                    {
                      "type": "JAction",
                      "options": {
                        "text": "关闭",
                        "level": "default",
                        "type": "close",
                        "close": true
                      }
                    }
                  ]
                }
              }
            },
            {
              "type": "JAction",
              "options": {
                "text": "删除",
                "level": "link",
                "icon": "close",
                "confirmMsg": "确定要删除？",
                "type": "ajax",
                "api": {
                  "url": "http://jap-mock-data.jd.local/appList/${id}",
                  "cache": 0,
                  "silent": false,
                  "data": {},
                  "dataType": "json",
                  "method": "delete",
                  "replaceData": false,
                  "sendOn": "",
                  "headers": {},
                  "requestAdaptor": "",
                  "adaptor": "",
                  "responseType": "",
                  "blob": ""
                },
                "reload": "crud"
              }
            }
          ],
          "name": "op"
        }
      ],
      "toolbar": [
        {
          "type": "JAction",
          "options": {
            "text": "添加应用",
            "level": "primary",
            "icon": "plus",
            "type": "dialog",
            "dialog": {
              "type": "JDialog",
              "options": {
                "title": "标题",
                "okText": "确定",
                "cancelText": "关闭"
              },
              "header": [],
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
                    "api": {
                      "url": "http://jap-mock-data.jd.local/appList",
                      "cache": 0,
                      "silent": false,
                      "data": {
                        "&": "$$"
                      },
                      "dataType": "json",
                      "method": "post",
                      "replaceData": false,
                      "sendOn": "",
                      "headers": {},
                      "requestAdaptor": "",
                      "adaptor": "",
                      "responseType": "",
                      "blob": ""
                    }
                  },
                  "columns": [
                    {
                      "type": "JFormItem",
                      "name": "app_name",
                      "componentType": "JInput",
                      "options": {
                        "label": "应用名称",
                        "placeholder": "请输入",
                        "labelCol": {},
                        "wrapperCol": {},
                        "required": true
                      }
                    },
                    {
                      "type": "JFormItem",
                      "name": "app_dept",
                      "componentType": "JInput",
                      "options": {
                        "label": "应用部门",
                        "placeholder": "请输入",
                        "labelCol": {},
                        "wrapperCol": {},
                        "required": true
                      }
                    },
                    {
                      "type": "JFormItem",
                      "name": "app_desc",
                      "componentType": "JInput",
                      "options": {
                        "label": "应用描述",
                        "placeholder": "请输入",
                        "labelCol": {},
                        "wrapperCol": {},
                        "required": true
                      }
                    },
                    {
                      "type": "JFormItem",
                      "name": "app_status",
                      "componentType": "JSelect",
                      "options": {
                        "label": "应用状态",
                        "dynamic": false,
                        "allowClear": true,
                        "options": [
                          {
                            "value": 1,
                            "label": "启用"
                          },
                          {
                            "value": 2,
                            "label": "禁用"
                          }
                        ],
                        "labelCol": {},
                        "wrapperCol": {},
                        "required": true
                      }
                    }
                  ]
                }
              ],
              "footer": [
                {
                  "type": "JAction",
                  "options": {
                    "text": "确定",
                    "level": "primary",
                    "type": "submit",
                    "close": true,
                    "reload": "crud"
                  }
                },
                {
                  "type": "JAction",
                  "options": {
                    "text": "关闭",
                    "level": "default",
                    "type": "close",
                    "close": true
                  }
                }
              ]
            }
          }
        }
      ],
      "options": {
        "cardOptions": {},
        "initApi": {
          "url": "http://jap-mock-data.jd.local/appList?_page=${pageNo}&_limit=${pageSize}&id=${app_id}&app_name=${app_name}&_sort=${orderBy}&_order=${orderDir}",
          "cache": 0,
          "silent": false,
          "data": {},
          "dataType": "json",
          "method": "get",
          "replaceData": false,
          "sendOn": "",
          "headers": {},
          "requestAdaptor": "",
          "adaptor": "",
          "responseType": "",
          "blob": ""
        },
        "key": "app_id",
        "tableModel": "items",
        "id": "crud"
      }
    }
  ],
  "toolbar": []
}
```

你可以在[低代码示例应用](http://beta-jap.jd.com/manage.html#/dev/app-detail/1410938307064725506?color=%236C71E9&theme=default&icon=&menuId=1426092104644657154)中体验它，并了解完整的配置
