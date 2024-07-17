# 服务端 API
可用范围
- 页面 api 里可直接调用
- 服务端调用时需要传递密钥

## 调用说明
接口访问格式
```
${京易域名}/app/biz/ + 接口路径。
```

例如
```
http://jap.jd.com/app/biz/formdata/api/getData
```

请求的 Content-Type 为 `application/json`

## 认证
### 页面 api
需要传递 header `X-Access-Token`，值为 `${__token}`

例如 ![](../assets/img/1.png)

### 服务端调用
TODO


## 表单记录
### 新增表单记录
- 路径: `formdata/api/add`
- 请求类型：`POST`

请求参数

| 字段名称 | 类型   | 必填 | 作用        |
|----------|------|-----|-----------|
| appId    | 字符串 | 是   | 应用 id     |
| tableId  | 字符串 | 是   | 数据模型 id |
| fields   | 对象   | 是   | 表单的数据  |

请求参数示例

```json
{
  "appId": "1391655327039184898",
  "tableId": "10002401",
  "fields": {
    "appCN": "系统C",
    "appEN": "systemc",
    "level": "3",
    "appOwner": "小刘",
    "department": "小小部"
  }
}
```

响应参数

| 字段名称    | 类型   | 必填 | 作用             |
|-------------|------|-----|----------------|
| create_by   | 字符串 | 是   | 创建人           |
| create_time | 字符串 | 是   | 创建时间         |
| update_by   | 字符串 | 是   | 最后更新人       |
| update_time | 字符串 | 是   | 最后更新时间     |
| id          | 字符串 | 是   | 表单记录唯一标识 |


响应参数示例

```json
{
  "success": true,
  "msg": "添加成功!",
  "status": 0,
  "data": {
    "create_by": "yemeng1",
    "appEN": "systemc",
    "update_time": "2021-05-12 16:32:47",
    "create_time": "2021-05-12 16:32:47",
    "level": 3,
    "delete_status": 0,
    "appCN": "系统C",
    "appId": "1391655327039184898",
    "appOwner": "小刘",
    "id": "10001387",
    "department": "小小部",
    "update_by": "yemeng1"
  },
  "timestamp": 1620808367851
}
```

### 删除表单记录
- 路径: `formdata/api/del`
- 请求类型: `POST`

请求参数

| 字段名称 | 类型   | 必填 | 作用                         |
|----------|------|-----|----------------------------|
| appId    | 字符串 | 是   | 应用 id                      |
| tableId  | 字符串 | 是   | 数据模型 id                  |
| ids      | 字符串 | 是   | 第几页, 多个值以英文逗号分割 |

请求参数示例

```json
{
  "ids": "10001413",
  "appId": "1391655327039184898",
  "tableId": "10002401"
}
```

响应参数示例

```json
{
  "success": true,
  "msg": "删除成功!",
  "status": 0,
  "data": null,
  "timestamp": 1620807717447
}
```


### 修改表单记录
- 路径: `formdata/api/update`
- 请求类型: `POST`

| 字段名称 | 类型   | 必填 | 作用        |
|----------|------|-----|-----------|
| appId    | 字符串 | 是   | 应用 id     |
| tableId  | 字符串 | 是   | 数据模型 id |
| fields   | 对象   | 是   | 表单的数据  |
| id       | 字符串 | 是   | 表单记录 id |

请求参数示例
```json
{
  "id": "10001381",
  "appId": "1391655327039184898",
  "tableId": "10002401",
  "fields": {
    "appOwner": "小刘2",
    "appCN": "系统A",
    "appEN": "systema",
    "level": 3,
    "department": "小小部"
  }
}
```

响应参数

| 字段名称    | 类型   | 必填 | 作用             |
|-------------|------|-----|----------------|
| create_by   | 字符串 | 是   | 创建人           |
| create_time | 字符串 | 是   | 创建时间         |
| update_by   | 字符串 | 是   | 最后更新人       |
| update_time | 字符串 | 是   | 最后更新时间     |
| id          | 字符串 | 是   | 表单记录唯一标识 |

响应参数示例
```json
{
  "success": true,
  "msg": "修改成功!",
  "status": 0,
  "data": {
    "create_by": "yemeng1",
    "appEN": "systema",
    "update_time": "2021-05-12 16:43:54",
    "create_time": "2021-05-11 18:13:34",
    "level": 3,
    "delete_status": 0,
    "appCN": "系统A",
    "appId": "1391655327039184898",
    "appOwner": "小刘2",
    "id": "10001381",
    "department": "小小部",
    "update_by": "yemeng1"
  },
  "timestamp": 1620809034128
}
```


### 根据 id 查询表单记录详情
- 路径: `formdata/api/getById`
- 请求类型: `POST`

请求参数

| 字段名称 | 类型   | 必填 | 作用        |
|----------|------|-----|-----------|
| appId    | 字符串 | 是   | 应用 id     |
| tableId  | 字符串 | 是   | 数据模型 id |
| id       | 字符串 | 是   | 表单记录 id |

请求参数示例
```json
{
  "appId": "1391655327039184898",
  "tableId": "10002401",
  "id": "10001381"
}
```

响应参数

| 字段名称    | 类型   | 必填 | 作用             |
|-------------|------|-----|----------------|
| create_by   | 字符串 | 是   | 创建人           |
| create_time | 字符串 | 是   | 创建时间         |
| update_by   | 字符串 | 是   | 最后更新人       |
| update_time | 字符串 | 是   | 最后更新时间     |
| id          | 字符串 | 是   | 表单记录唯一标识 |

响应参数示例
```json
{
  "success": true,
  "msg": "获取成功!",
  "status": 0,
  "data": {
    "create_by": "yemeng1",
    "appEN": "systema",
    "update_time": "2021-05-11 20:19:48",
    "create_time": "2021-05-11 18:13:34",
    "level": 3,
    "delete_status": 0,
    "appCN": "系统A",
    "appId": "1391655327039184898",
    "appOwner": "小刘1",
    "id": "10001381",
    "department": "小小部",
    "update_by": "yemeng1"
  },
  "timestamp": 1620808831700
}
```

### 根据条件搜索表单记录详情列表
- 路径：`formdata/api/getData`
- 请求类型：`POST`

请求参数

| 字段名称 | 类型   | 必填 | 作用         |
|----------|------|-----|------------|
| appId    | 字符串 | 是   | 应用 id      |
| tableId  | 字符串 | 是   | 数据模型 id  |
| pageNo   | 整形   | 是   | 第几页       |
| pageSize | 整形   | 是   | 每页几条数据 |
| fields   | 对象   | 是   | 查询条件     |

请求参数示例

没有查询条件时
```json
{
  "appId": "1391655327039184898",
  "tableId": "10002401",
  "pageNo": 1,
  "pageSize": 10,
  "fields": {}
}
```

有 1 个查询条件时
```json
{
  "appId": "1391655327039184898",
  "tableId": "10002431",
  "pageNo": 1,
  "pageSize": 10,
  "fields": {
    "appCN": "系统A"
  }
}
```

响应参数

| 字段名称 | 类型 | 必填 | 作用         |
|----------|----|-----|------------|
| total    | 整形 | 是   | 记录个数     |
| pageNo   | 整形 | 是   | 第几页       |
| pageSize | 整形 | 是   | 每页几条数据 |
| rows     | 数组 | 是   | 数据         |

响应示例

```json
{
  "success": true,
  "msg": "操作成功！",
  "status": 0,
  "data": {
    "total": 1,
    "pageNo": 1,
    "pageSize": 10,
    "rows": [
      {
        "create_by": "yemeng1",
        "appEN": "systema",
        "update_time": "2021-05-11 18:13:34",
        "create_time": "2021-05-11 18:13:34",
        "level": 3,
        "delete_status": 0,
        "appCN": "系统A",
        "appId": "1391655327039184898",
        "appOwner": "小刘",
        "id": "10001381", 
        "department": "小小部",
        "update_by": "yemeng1"
      }
    ]
  },
  "timestamp": 1620728172219
}
```


## 附录
### 保存/更新/搜索 表单记录数据格式说明
表单 input 控件的数据放在 fields 字段下，每个 input 控件的值格式如下

| 组件类型   | 数据格式 | 示例                                                                                  | 备注                       |
|----------|--------|---------------------------------------------------------------------------------------|----------------------------|
| 输入框     | 字符串   | "aaa"                                                                                 |                            |
| 文本域     | 字符串   | "aaa"                                                                                 |                            |
| 数值       | 数字     | 1                                                                                     |                            |
| 金额       | 数字     | 22                                                                                    |                            |
| 上传图片   | 字符串   | "https://storage.jd.com/apaaspub/d389f95ff2f0431fbceb82deb0e0c4b6_1620812389059.jpeg" | 图片地址                   |
| 日期选择   | 字符串   | "2021-05-12"                                                                          |                            |
| 下拉单选   | 字符串   | "a"                                                                                   | 这里值的格式由下拉选项决定 |
| 日期范围   | 数组     | ["2021-05-12", "2021-06-14"]                                                          |                            |
| 评分       | 数字     | 1                                                                                     |                            |
| 单选框     | 数字     | 2                                                                                     |                            |
| 多选框     | 字符串   | "[1, 2]"                                                                              |                            |
| 滑动输入条 | 数字     | 41                                                                                    |                            |
| 开关       | 数字     | 1                                                                                     | 开为1，关为 -1              |