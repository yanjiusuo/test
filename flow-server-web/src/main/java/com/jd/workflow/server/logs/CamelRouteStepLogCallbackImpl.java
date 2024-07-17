package com.jd.workflow.server.logs;

import com.jd.businessworks.callback.StepLogCallback;
import com.jd.businessworks.log.StepLogMessage;
import com.jd.businessworks.register.RuntimeRegister;
import com.jd.workflow.console.service.AbstractLogCollectService;
import com.jd.workflow.console.service.LogCollector;
import com.jd.workflow.console.service.LogEntity;
import com.jd.workflow.server.dao.CamelStepLogMapper;
import com.jd.workflow.server.entity.CamelStepLogEntity;
import com.jd.workflow.server.enums.LogLevelEnum;
import com.jd.workflow.server.service.CamelStepLogService;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CamelRouteStepLogCallbackImpl extends AbstractLogCollectService implements StepLogCallback, InitializingBean {
    @Value("${flow.log.enabled:true}")
    private boolean enableLog = true;
    @Autowired(required = false)
    private CamelStepLogService camelStepLogMapper;
    @Override
    public void afterPropertiesSet() throws Exception {
        RuntimeRegister.registerStepLogCallback(this);
        this.enabled = true;

        this.logCollector = new LogCollector() {
            @Override
            public void collect(List<LogEntity> list) {
                List<CamelStepLogEntity> logs = list.stream().map(vs -> toLog(vs)).collect(Collectors.toList());
                camelStepLogMapper.saveBatch(logs);
            }
        };
        super.start();
    }

    public CamelStepLogEntity toLog(LogEntity logEntity){
        CamelStepLogEntity stepLog = new CamelStepLogEntity();
        stepLog.setMethodId(logEntity.getMethodName()+"");
        stepLog.setCreated(new Date());
        stepLog.setBusinessId(logEntity.getBusinessName()+"");
        if(logEntity.isError()){
            stepLog.setLogLevel(LogLevelEnum.EXCEPTION.getCode());
        }else{
            stepLog.setLogLevel(LogLevelEnum.NONE.getCode());
        }
        stepLog.setLogContent(JsonUtils.toJSONString(logEntity.getMsg()));
        stepLog.setVersion(logEntity.getPublishVersion());
        return stepLog;
    }

    @Override
    public void log(Object obj) {
        if(!enableLog) return;
        try {
            if (obj != null && obj instanceof StepLogMessage) {
                StepLogMessage stepLogMessage = (StepLogMessage) obj;
                Object data = stepLogMessage.getData();

                boolean isError = false;
                if(data != null && data instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) data;

                    isError = dataMap.get("exception") != null;
                }
                LogEntity entity = new LogEntity();
                entity.setMethodName(stepLogMessage.getMethodName());
                entity.setBusinessName(stepLogMessage.getBusinessName());
                entity.setPublishVersion(stepLogMessage.getVersion());
                entity.setError(isError);
                entity.setMsg(data);
                addLog(entity);

            } else {
                log.error("CamelRouteStepLogCallbackImpl#log 日志类型异常: " + (obj == null ? null : obj.getClass()));
            }
        } catch (Exception e) {
            log.error("CamelRouteStepLogCallbackImpl#log 日志保存失败: ", e);
        }
    }


}
