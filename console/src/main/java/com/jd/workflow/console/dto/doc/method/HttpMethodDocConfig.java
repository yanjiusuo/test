package com.jd.workflow.console.dto.doc.method;

import com.jd.jsf.gd.config.MethodConfig;
import lombok.Data;

@Data
public class HttpMethodDocConfig extends MethodDocConfig {




    public String getType() {
        return TYPE_HTTP;
    }
}
