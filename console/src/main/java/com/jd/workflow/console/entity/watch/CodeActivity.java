package com.jd.workflow.console.entity.watch;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.watch.dto.BuildTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;

@Data
@TableName(value = "code_activity", autoResultMap = true)
public class CodeActivity extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 操作时间
     */
    private Timestamp time;
    /**
     * erp
     */
    private String erp;
    /**
     * 系统用户名
     */
    private String userName;
    /**
     * 工程
     */
    private String project;
    /**
     * 代码仓库
     */
    String codeRepository;
    /**
     * 分支
     */
    private String branch;
    /**
     * 事件类型
     */
    String eventType;
    /**
     * 语言
     */
    private String language;
    /**
     * build、heartbeat、run
     */
    private String type;
    /**
     *  子类型
     */
    private String subType;
    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 是否保存
     */
    private Integer isWrite;
    /**
     * 操作行号
     */
    public Integer lineNumber;
    /**
     * 行数量
     */
    private Integer lineCount;
    public Integer cursorPosition;
    /**
     * 构建耗费时间
     */
    private Integer costTime;
    /**
     * 是否全量构建
     */
    BuildTypeEnum buildType;
    /**
     * 构建是否成功
     */
    private Integer buildSuccess;
    /**
     * 构建文件数量
     */
    private int buildFileCount;
    /**
     * 频道:cjg、joycoder
     */
    private String channel;
    /**
     * 完整的git堆栈
     */
    private String fullGitInfo;
}
