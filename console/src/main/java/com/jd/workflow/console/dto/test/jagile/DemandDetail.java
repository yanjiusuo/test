package com.jd.workflow.console.dto.test.jagile;

import com.jd.workflow.metrics.client.DemandUserResponse;
import com.jd.workflow.metrics.client.UserResponse;
import lombok.Data;

import java.sql.Date;

@Data
public class DemandDetail {
    Long id;
    String demandCode;
    String name;
    String pmpProjectId;
    String pmpProjectName;
    String pmpProjectCode;
    String recipientErp;
    DemandUserResponse relatedUsers;
    /**
     * 提交人
     */
    UserResponse proposer;
    /**
     * 创建time
     */
    Date cTime;



}
