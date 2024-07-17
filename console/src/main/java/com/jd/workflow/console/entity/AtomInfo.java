package com.jd.workflow.console.entity;

import lombok.Data;

/**
 * http://rdpe.jd.com/upipe/api/bundle-service/appendix/#324-%E8%A7%A6%E5%8F%91%E7%BB%84%E4%BB%B6%E5%AE%9E%E4%BE%8B
 */
@Data
public class AtomInfo {
    private String atomId;
    private String aliasName;
    private String atomImage;
    private String atomVersion;
}
