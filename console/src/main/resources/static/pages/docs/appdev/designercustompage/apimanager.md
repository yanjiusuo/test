# API 管理

## 数据映射
![](../../assets/img/appdev/designercustompage/1.png)

这里可以引用[变量](#变量)

不管是 GET 请求的 query parameters，还是 POST 请求的 body，都是在数据映射里面配置请求参数

GET 请求配置

![](../../assets/img/appdev/designercustompage/5.png)

POST 请求

![](../../assets/img/appdev/designercustompage/6.png)


### 获取应用 id 和数据模型 idå
```json
{
  "appId": "${__appId}",
  "tableId": "${__tableId}"
}
```

上述配置可以获得如下 json
```json
{
  "appId": "1391655327039184898",
  "tableId": "10003909"
}
```

### 引用数字
```json
{
  "id": "${id}",
  "apaasappid": "1374635028637712385",
  "appCn": "${appCn}",
  "appEn": "${appEn}",
  "level": "${level}",
  "appOwner": "${appOwner}",
  "department": "${department}"
}
```

假设表单里有以下控件
1. appCn/appEn/appOwner/department 是输入框控件
2. level 是数值控件

其中 `${level}` 是数字，数据映射会将 `"${level}"` 变成数字，上述配置可以获得如下 json

```json
{
  "id": "1392741565767766018",
  "apaasappid": "1374635028637712385",
  "appCn": "系统A",
  "appEn": "systema",
  "level": 3,
  "appOwner": "小明",
  "department": "部门A"
}
```

### 引用表单里所有字段
```json
{
  "id": "${id}",
  "appId": "${__appId}",
  "tableId": "${__tableId}",
  "fields": "$$"
}
```

假设表单里有两个输入框控件 name, email，那么到时候发送的数据就是

```json
{
  "id": "1392741565767766018",
  "appId": "1391655327039184898",
  "tableId": "10003909",
  "fields": {
      "name": "小明",
      "email": "sss@demo.com"
  }
```

### 展开所配置的数据
```json
"name": "${name}",
"email": "${email}",
"&": "${c}"
```

假设 c 变量的值为
```json
{
  "e": "3",
  "f": "4",
  "g": "5"
}
```

那么到时候发送的数据就是
```json
{
  "name": "小明",
  "email": "xiaoming@demo.com",
  "e": "3",
  "f": "4",
  "g": "5"
}
```

### API 调试
打开调试抽屉

![](../../assets/img/appdev/designercustompage/2.png)

可以注入变量

![](../../assets/img/appdev/designercustompage/3.png)

点击调试按钮发起网络请求，如果是 Chrome 浏览器，可以在网络选项卡里面看到网络请求

![](../../assets/img/appdev/designercustompage/4.png)

## 附录
### 内置页面变量
只能在 api 和表达式里面用

- `pageNo`：第几页
- `pageSize`：一页有多少条蜀山
- `orderBy`：用于排序的字段，只能一个字段
- `orderDir`: 排序，值为 `asc` 或者 `desc`

提示：搜索框里的表单里的字段也是可以在 api 里面用的

### 内置全局变量
每个地方都能用

- `__token`，调用平台的 API 需要用到
- `__tenantId`，获取当前的租户id
- `__appId`，获取当前的应用 id
- `__tableId`，获取当前的数据模型 id

#### 表单记录变量
可以将表单里控件的值通过控件的字段标识引用