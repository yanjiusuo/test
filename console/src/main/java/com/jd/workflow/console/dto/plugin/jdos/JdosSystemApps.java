package com.jd.workflow.console.dto.plugin.jdos;

import com.jd.workflow.console.dto.plugin.jdos.JdosApps;
import lombok.Data;

import java.util.List;

/**
 * @author by lishihao
 * @date 2021/3/10
 * DESC TODO
 */
@Data
public class JdosSystemApps {
    /**
     *
     */
    private String id;
    /**
     *
     */
    private String systemName;
    /**
     *
     */
    private String nickname;
    /**
     * 租户
     */
    private String tenant;
    /**
     *
     */
    private String desc;
    /**
     *
     */
    private String creator;
    /**
     *
     */
    private String createTime;
    /**
     *
     */
    private String systemLevel;
    /**
     *
     */
    private String systemOwner;
    /**
     *
     */
    private String systemPm;
    /**
     *
     */
    private String systemQa;
    /**
     *
     */
    private String systemDepartment;
    /**
     *
     */
    private List<JdosApps> apps;
    /**
     *
     */
    private String updateTime;
    /**
     *
     */
    private boolean fromApi;
    /**
     *
     */
    private String type;
    /**
     *
     */
    private String systemId;
    /**
     *
     */
    private String levelOneDepartmentName;
    /**
     *
     */
    private String levelTwoDepartmentName;
    /**
     *
     */
    private String levelName;
    /**
     *
     */
    private String systemOwnerName;
    /**
     *
     */
    private String systemDepartmentId;
    /**
     *
     */
    private String umpApps;
    /**
     *
     */
    private String systemAdminList;
    /**
     *
     */
    private String systemLevelDesc;

}
