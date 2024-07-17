package com.jd.workflow.console.service.debug;

import com.jd.workflow.console.dto.flow.param.JsfOutputExt;
import com.jd.workflow.console.dto.jsf.JarJsfDebugDto;
import com.jd.workflow.jsf.analyzer.MavenJarLocation;
import com.jd.workflow.jsf.input.JsfOutput;

public interface IJsfJarCallService {

    public  boolean reParseJsfJar(MavenJarLocation location);

    public JsfOutputExt jarCallJsf(JarJsfDebugDto dto);
}
