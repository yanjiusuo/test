package com.jd.workflow.console.entity.watch.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
public class CodeActivityDto {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 操作时间
     */
    @NotNull
    private long time;
    /**
     * erp
     */

    private String erp;
    /**
     * 系统用户名
     */
    @NotNull
    private String userName;
    /**
     * 工程
     */
    @NotNull
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
     * build or heartbeat
     */
    @NotNull
    private String type;

    private String subType;
    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 是否保存
     */

    private boolean isWrite;
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
    private boolean buildSuccess;
    /**
     * 构建文件数量
     */
    private int buildFileCount;

    private String channel;
    private String fullGitInfo;
}
