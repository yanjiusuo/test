# 代码导出
所有线上的代码均支持代码导出功能，详情参考：https://coding.jd.com/wangjingfang3/lowcode-render/

代码可以通过线上的代码进行导出。

# 控件设计逻辑


# 自定义代码
当现有逻辑不能满足要求时，可以自定义代码，不同控件提供了不同的api可供使用，通过env.getCompById()获取组件示例，然后可以调用对应的方法

## 表单
表单提供如下访问

| 方法                  | 说明                                                         | 类型                                                    |
| --------------------- | ------------------------------------------------------------ | ------------------------------------------------------- |
| validateForm          | 校验表单字段,options结构为：`{showError:boolean}`，表示是否显示错误。校验成功返回当前表单的值，校验失败返回`{errors:Array<String>}`错误对象 | `([fieldNames],[options])=>Promise`                     |
| refresh               | 刷新当前表单：会重新调用初始数据接口                         | ()=>Promise                                             |
| setFieldsValue        | 设置字段值                                                   | ({[fieldName]:[value:any]},triggerChange:boolean)=>void |
| getFieldsValue        | 获取字段值,传入字段列表,不传获取所有字段数据                 | (Array<string>)=>Object                                 |
| getData               | 获取当前数据域数据                                           | ()=>obj                                                 |
| getFormInitialValues  | 获取表单初始化数据                                           | Function(){}                                            |
| resetValues           | 重置表单数据，设置为初始值                                   | ()=>void                                                |
| sendToTarget          | 发送表单数据到指定数据                                       | ()=>void                                                |
| sendTo(targetId,data) | 发送数据到指定控件                                           | ()=>void                                                |
| submit                | 提交表单数据，返回Promise对象                                |                                                         |
| getFormFields         | 获取当前表单里有的数据                                       |                                                         |

## 增删改查

| 方法             | 说明               | 类型        |
| ---------------- | ------------------ | ----------- |
| refresh          | 刷新列表数据       | ()=>Promise |
| getData          | 获取当前数据列表   | ()=>Object  |
| setFieldsValue   | 设置表格里可用变量 |             |
| getSelectedItems | 获取选中记录       |             |
| clearSelection   | 清除选中状态       |             |

## 对话框

| 方法  | 说明       | 类型     |
| ----- | ---------- | -------- |
| open  | 打开对话框 | ()=>void |
| close | 关闭对话框 | ()=>void |

## 环境变量函数

提供`env`环境变量，用来操作与环境有关的操作

| 方法     | 说明                                                         | 类型                                                         |
| -------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| jumpTo   | 跳转页面,to可以是路由地址或者网址，当为路由地址时用路由跳转，否则进行页面替换 | (to)=>void                                                   |
| notify   | 弹出提示框信息，type为`success`、`error`、`warning`、`info`  | (type,msg,{timeout:int})=> void                              |
| fetcher  | 获取数据                                                     | ({method,url,data,headers})=>Promise<Response>               |
| toast    | toast提示,type为success、info、warning、error、loading       | ({type,icon,content,duration})=>void                         |
| dialog   | dialog提示对话框,更多具体参考[ant对话框配置](https://www.antdv.com/components/modal-cn/#components-modal-demo-customize-footer-buttons-props),type为info、success、error、warning、confirm | ({type,title,content,icon,width,okText,okType,onOk,onCancel})=>void |
| onAction | 执行action，如打开一个对话框,该操作与按钮里配置的行为一致    | (type,options)=>void                                         |
| confirm  | 发出提示信息  ,更多具体参考[ant对话框配置](https://www.antdv.com/components/modal-cn/#components-modal-demo-customize-footer-buttons-props), | ({title,content,onOk(){},onCancel(){} })                     |

示例：

```js
env.onAction('dialog',{
   
    "dialog": {
      "type": "JDialog",
      "options": {
        "title": "标题",
        "okText": "确定",
        "cancelText": "关闭"
      },
      "header": [],
      "body": [
        
      ],
      "footer": [
        {
          "type": "JAction",
          "options": {
            "text": "确定",
            "level": "primary",
            "type": "submit",
            "close": true
          }
        } 
      ]
    }
  })
env.dialog({
 type:'info',
  title:'标题',content:'内容'
})
env.notify('error','出错了')
env.toast({
 type:'error',
  content:'出错了'
})
env.confirm({
 title:'标题',
  content:'内容'
})
```



## 工具函数

提供工具类**jui**，上面可以使用工具函数

| 分类     | 方法           | 说明                                                         | 类型                     |
| -------- | -------------- | ------------------------------------------------------------ | ------------------------ |
| 类型判断 | isObject       | 是否对象                                                     | (obj)=>boolean           |
| 类型判断 | isArray        | 是否数组                                                     | (obj)=>boolean           |
| 类型判断 | isFunction     | 是否函数                                                     | (obj)=>boolean           |
| 类型判断 | isString       | 是否字符串                                                   | (obj)=>boolean           |
| 类型判断 | isNumber       | 是否数字                                                     | (obj)=>boolean           |
| 类型判断 | isStringNumber | 是否可转换为数字，如字符串`"1"`，`1`都为true                 | (obj)=>boolean           |
| 类型判断 | isBoolean      | 是否boolean类型                                              | (obj)=>boolean           |
| 类型判断 | isEmptyValue   | 判断是否为空值，`[]`、`{}`、`null`、`undefined`、`""`这五种结果返回true，其余为false | (obj)=>boolean           |
| 对象操作 | clone          | 数据拷贝,不指定deep,相当于Object.assign()赋值，指定deep的话会深度拷贝 | (obj,[deep])=>obj        |
| 对象     | getPropValue   | 获取对象数据，支持嵌套，如`getPropValue({a:{b:1}},'a.b')`返回1 | (obj,key)=>{}            |
| 字符串   | startsWith     | 判断str是否以str开头                                         | (str,sub)=>boolean       |
| 字符串   | endsWith       | 判断str是否以str结尾                                         | (str,sub)=>boolean       |
| 字符串   | formatDate     | 格式化日期,patten为模板，如`yyyy-MM-dd HH:mm:ss`             | (date,pattern)=>str      |
| 字符串   | firstPart      | 字符串分割的第一部分,如`firstPart('a.b','.')`返回a           | (str,separator)=>str     |
| 字符串   | lastPart       | 字符串分割的后一部分,字符串的第一部分,如`firstPart('a.b','.')`返回b | (str,separator)=>str     |
| 数组     | findByKey      | 根据key查找数据，返回第一项或者空                            | (arr,key,value)=>obj     |
| 数组     | groupBy        | 按key进行分组，返回按key分组后的数据                         | (arr,key)=>{[key]:Array} |
| 数组     | sum            | 按key累加                                                    | (arr,key)=>Number        |

# 自定义提交或者取值逻辑

当api需要的提交或者取值逻辑不满足需求时，可以通过自定义数据初始化逻辑或者提交逻辑

# 引入动态依赖库

当工具函数不满足需求时，可以引用`nodejs`动态依赖包作为工具类

