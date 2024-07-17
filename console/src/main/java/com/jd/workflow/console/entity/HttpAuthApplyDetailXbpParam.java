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
public class HttpAuthApplyDetailXbpParam extends AbstractXbpTicket implements Serializable {
    private static final long serialVersionUID = 2434663802925076341L;
    @XbpTicket.TableInfo.Column(name = "接口名称", needDeserialize = true)
    private String methodInfo;

    @XbpTicket.TableInfo.Column(name = "路径", needDeserialize = true)
    private String path;

    @XbpTicket.TableInfo.Column(name = "项目名称", needDeserialize = true)
    private String interfaceInfo;

    @XbpTicket.TableInfo.Column(name = "鉴权标识", needDeserialize = true)
    private String authCode;
}
