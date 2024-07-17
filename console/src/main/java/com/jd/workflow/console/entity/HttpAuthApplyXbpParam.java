package com.jd.workflow.console.entity;

import com.jd.flow.xbp.AbstractXbpTicket;
import com.jd.flow.xbp.annotation.XbpTicket;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhibeibei3
 */
@Getter
@Setter
@ToString
@XbpTicket
public class HttpAuthApplyXbpParam extends AbstractXbpTicket implements Serializable {

    private static final long serialVersionUID = 2434663802925076341L;

    @XbpTicket.ApplicationInfo(itemName = "申请接口所属应用")
    private String appInfo;

    @XbpTicket.ApplicationInfo(itemName = "调用方应用")
    private String callAppInfo;

    @XbpTicket.ApplicationInfo(itemName = "申请陈述")
    private String applyDesc;

    @XbpTicket.TableInfo(tableName = "申请鉴权接口列表", needDeserialize = true)
    private List<HttpAuthApplyDetailXbpParam> methodInfoList;

    @XbpTicket.ApplicationFlow(flowName = "产品审批人")
    private List<String> productApprovers;


    @XbpTicket.ApplicationFlow(flowName = "研发审批人")
    private List<String> devApprovers;



}
