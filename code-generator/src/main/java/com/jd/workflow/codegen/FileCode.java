package com.jd.workflow.codegen;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class FileCode {
    /**
     * 代码
     */
    String code;
    /**
     * 文件名称
     */
    String fileName;
    /**
     * 文件路径
     */
    @JsonIgnore
    String filePath;
}
