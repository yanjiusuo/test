# 设计新增记录对话框
进入设计器页面

![](../../assets/img/tutorial-1/1.png)

先拖一个增删改查布局控件出来

![](../../assets/img/tutorial-1/2.png)

拖一个按钮出来

![](../../assets/img/tutorial-1/3.png)

点击拖出来的按钮，出现按钮的属性面板，将文本改为 `新增记录`

![](../../assets/img/tutorial-1/4.png)

属性面板向下滚动，将类型设为`打开对话框`

![](../../assets/img/tutorial-1/5.png)

点击配置对话框按钮

![](../../assets/img/tutorial-1/6.png)

出现对话框配置页面，在这里设计好新增记录的表单

![](../../assets/img/tutorial-1/7.png)

先拖出来表单布局控件

![](../../assets/img/tutorial-1/8.png)

删除提交按钮

![](../../assets/img/tutorial-1/9.png)

将标签设为`系统中文名`, 数据字段设为 `appCn`

![](../../assets/img/tutorial-1/10.png)

依次拖出剩余3个输入框，标签和数据字段分别为
- 系统英文名, appEn
- 系统负责人，appOwner
- 所属部门，department

![](../../assets/img/tutorial-1/11.png)

拖出来一个单选框

![](../../assets/img/tutorial-1/12.png)

将标签改为系统级别，数据字段改为 level

![](../../assets/img/tutorial-1/13.png)

设置单选框的选项，选项如下
- 外围系统(3级), 3
- 业务支撑系统(2级), 2
- 业务运营系统(1级), 1
- 电商核心系统(0级), 0

效果如下图

![](../../assets/img/tutorial-1/14.png)

将`确定`按钮的类型设为`提交`
![](../../assets/img/tutorial-1/15.png)

将`关闭`按钮的类型设为`关闭对话框`
![](../../assets/img/tutorial-1/16.png)

将表单的数据提交 api 设为 `新建记录`

![](../../assets/img/tutorial-1/18.png)

点击确定按钮，保存对话框的设计
![](../../assets/img/tutorial-1/17.png)