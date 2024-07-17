# demo API 附录
调用说明
- baseUrl: http://beta-jap.jd.com/biz/
- header, Key: `X-Access-Token` value: `"${__token}"`

### 获取列表记录
- 请求方法：GET
- 路径：demo/crud/list

请求参数


| 字段名称   | 类型   | 必填 | 作用             |
|------------|------|-----|----------------|
| apaasappid | 字符串 | 是   | 应用 id          |
| appCn      | 字符串 | 否   | 系统名称         |
| pageSize   | 数字   | 是   | 一页有多少条记录 |
| pageNo     | 数字   | 是   | 第几页           |

api 配置截图示例，其中，接口地址的域名请换成 `http://jap.jd.com/biz/demo/crud/list`

![](../../assets/img/tutorial-1/30.png)

其中 `pageSize` 和 `pageNo` 是页面的内置字段，分别代表分页大小和第几页

![](../../assets/img/tutorial-1/52.png)

其中默认的分页大小是从 list API 的响应的 pageSize 字段获取的
![](../../assets/img/tutorial-1/53.png)

请求参数示例

`http://beta-jap.jd.com/biz/demo/crud/list?apaasappid=1391655327039184898&appCn=系统C&`

响应参数

| 字段名称   | 类型   | 必填 | 作用       |
|------------|------|-----|----------|
| appEn      | 字符串 | 是   | 系统英文名 |
| appCn      | 字符串 | 是   | 系统中文名 |
| level      | 字符串 | 是   | 系统级别   |
| appOwner   | 字符串 | 是   | 系统负责人 |
| department | 字符串 | 是   | 所属部门   |

响应参数示例
```json
{
    "success": true,
    "msg": "操作成功！",
    "status": 0,
    "data": {
        "total": 2,
        "pageNo": 1,
        "pageSize": 10,
        "rows": [
            {
                "id": "1392727563062972418",
                "createBy": "admin",
                "createTime": "2021-05-13 14:25:01",
                "updateBy": "admin",
                "updateTime": "2021-05-13 14:25:01",
                "appEn": "systeme",
                "appCn": "系统E",
                "level": 2,
                "appOwner": "小刘",
                "department": "小小部",
                "apaasappid": "1391655327039184898"
            },
            {
                "id": "1392727729891414018",
                "createBy": "admin",
                "createTime": "2021-05-13 14:25:41",
                "updateBy": "admin",
                "updateTime": "2021-05-13 14:25:41",
                "appEn": "systeme",
                "appCn": "系统C",
                "level": 2,
                "appOwner": "小刘",
                "department": "小小部",
                "apaasappid": "1391655327039184898"
            }
        ]
    },
    "timestamp": 1620887500282
}
```

### 新建记录
- 请求方法：POST
- 路径：demo/crud/add

请求参数


| 字段名称   | 类型   | 必填 | 作用       |
|------------|------|-----|----------|
| apaasappid | 字符串 | 是   | 应用 id    |
| appEn      | 字符串 | 是   | 系统英文名 |
| appCn      | 字符串 | 是   | 系统中文名 |
| level      | 字符串 | 是   | 系统级别   |
| appOwner   | 字符串 | 是   | 系统负责人 |
| department | 字符串 | 是   | 所属部门   |

请求参数示例

```json
{
    "apaasappid": "1391655327039184898",
    "appCn": "系统E",
    "appEn": "systeme",
    "level": 2,
    "appOwner": "小刘",
    "department": "小小部"
}
```

api 配置截图示例，其中，接口地址的域名请换成 `http://jap.jd.com/biz/demo/crud/add`

![](../../assets/img/tutorial-1/31.png)

响应参数示例
```json
{
    "success": true,
    "message": "添加成功！",
    "code": 200,
    "result": null,
    "timestamp": 1620887168803
}
```

### 修改记录
- 请求方法：POST
- 路径：demo/crud/edit

请求参数


| 字段名称   | 类型   | 必填 | 作用       |
|------------|------|-----|----------|
| id         | 字符串 | 是   | 系统 id    |
| apaasappid | 字符串 | 是   | 应用 id    |
| appEn      | 字符串 | 是   | 系统英文名 |
| appCn      | 字符串 | 是   | 系统中文名 |
| level      | 字符串 | 是   | 系统级别   |
| appOwner   | 字符串 | 是   | 系统负责人 |
| department | 字符串 | 是   | 所属部门   |

请求参数示例

```json
{
    "apaasappid": "1391655327039184898",
    "appCn": "系统E",
    "appEn": "systeme",
    "level": 2,
    "appOwner": "小刘",
    "department": "小小部"
}
```

api 配置截图示例，其中，接口地址的域名请换成 `http://jap.jd.com/biz/demo/crud/edit`

![](../../assets/img/tutorial-1/51.png)

响应参数示例
```json
{
    "success": true,
    "message": "添加成功！",
    "code": 200,
    "result": null,
    "timestamp": 1620887168803
}
```
### 获取单条记录
- 请求方法：GET
- 路径：demo/crud/queryById

请求参数

| 字段名称 | 类型   | 必填 | 作用    |
|----------|------|-----|-------|
| id       | 字符串 | 是   | 系统 id |

请求参数示例
`http://beta-jap.jd.com/biz/demo/crud/queryById?id=1392727729891414018`

api 配置截图示例，其中，接口地址的域名请换成 `http://jap.jd.com/biz/demo/crud/queryById`

![](../../assets/img/tutorial-1/50.png)


响应参数

| 字段名称   | 类型   | 必填 | 作用       |
|------------|------|-----|----------|
| id         | 字符串 | 是   | 系统 id    |
| apaasappid | 字符串 | 是   | 应用 id    |
| appEn      | 字符串 | 是   | 系统英文名 |
| appCn      | 字符串 | 是   | 系统中文名 |
| level      | 字符串 | 是   | 系统级别   |
| appOwner   | 字符串 | 是   | 系统负责人 |
| department | 字符串 | 是   | 所属部门   |


响应参数示例
```json
{
    "success": true,
    "message": "操作成功！",
    "code": 200,
    "result": {
        "id": "1392727729891414018",
        "createBy": "admin",
        "createTime": "2021-05-13 14:25:41",
        "updateBy": "admin",
        "updateTime": "2021-05-13 15:14:17",
        "appEn": "systeme",
        "appCn": "系统E",
        "level": 2,
        "appOwner": "小刘1",
        "department": "小小部",
        "apaasappid": "1391655327039184898"
    },
    "timestamp": 1620890177710
}
```

### 单条删除记录
- 请求方法：POST
- 路径：demo/crud/delete

请求参数

| 字段名称 | 类型   | 必填 | 作用    |
|----------|------|-----|-------|
| id       | 字符串 | 是   | 系统 id |


请求参数示例
`http://beta-jap.jd.com/biz/demo/crud/delete?id=1392727729891414018`


api 配置截图示例，其中，接口地址的域名请换成 `http://jap.jd.com/biz/demo/crud/delete?id=${id}`

![](../../assets/img/tutorial-1/49.png)

响应参数示例
```json
{
    "success": true,
    "message": "删除成功!",
    "code": 200,
    "result": null,
    "timestamp": 1620890455023
}
```
