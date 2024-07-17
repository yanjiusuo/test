package com.jd.workflow.console.service.plugin.jdos;

import com.jd.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description:
 * @author: sunchao81
 * @Date: 2023-11-06
 */
@Slf4j
@Service
public class JdosFactoryService  {


    @Resource
    JdosJcd jdosJcd;

    @Resource
    JdosJcdPre jdosJcdPre;

    public JdosAbstract getService(String env) {
        log.info("getService env={}",env);
        try{
            if(StringUtils.isNotEmpty(env) && "pre".equals(env)){
                return jdosJcdPre;
            }else {
                return jdosJcd;
            }
        }catch(Exception e){
            log.error("getService",e);
            return jdosJcd;
        }
    }
}
