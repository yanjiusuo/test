package com.jd.workflow.console.service.ratelimiting;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jd.ump.profiler.proxy.Profiler;
import com.jd.workflow.console.dto.ratelimiting.OperateType;
import com.jd.workflow.console.dto.ratelimiting.RateLimitingChangeStatusDTO;
import com.jd.workflow.console.entity.RateLimitingRules;
import com.jd.workflow.console.entity.RateLimitingRulesConfig;
import com.jd.workflow.console.entity.RateLimitingRulesOperateLog;
import com.jd.workflow.console.service.RateLimitingRulesConfigService;
import com.jd.workflow.console.service.RateLimitingRulesOperateLogService;
import com.jd.workflow.console.service.RateLimitingRulesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Aspect
@Component
@Slf4j
public class RateLimitingLogAspect {

    @Autowired
    RateLimitingRulesOperateLogService operateLogService;

    @Autowired
    RateLimitingRulesService rateLimitingRulesService;

    @Autowired
    RateLimitingRulesConfigService rateLimitingRulesConfigService;

    //private static final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, 2, 1, TimeUnit.HOURS, new ArrayBlockingQueue<>(1000));

    /**
     * 增加规则日志
     */
    @Pointcut("@annotation(com.jd.workflow.console.service.ratelimiting.annotation.AddLog)")
    public void addLog() {
    }

    @Around("addLog()")
    public Object addLogAround(ProceedingJoinPoint joinPoint) throws Throwable {
        //执行业务实现方法
        Object result;
        try {
            Object[] args = joinPoint.getArgs();
            log.info("添加规则日志参数：" + Arrays.toString(args));
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw throwable;
        }

        //记录日志，捕获异常
        try {
            if (result instanceof List) {
                List<Long> list = new ArrayList<>();
                for (Object o : (List<?>) result) {
                    if (o instanceof Long) {
                        list.add((Long) o);
                    }
                }
                String erp = null;
                if (joinPoint.getArgs().length>=2){
                    Object[] args = joinPoint.getArgs();
                    if (args[1] instanceof String){
                        erp = (String) args[1];
                    }
                }

                long batchId = System.currentTimeMillis();
                List<RateLimitingRules> rateLimitingRules = rateLimitingRulesService.listByIds(list);
                Date date = new Date();
                List<RateLimitingRulesOperateLog> logList = new ArrayList<>();
                for (RateLimitingRules rule : rateLimitingRules) {
                    RateLimitingRulesOperateLog operateLog = new RateLimitingRulesOperateLog();
                    operateLog.setAppProvider(rule.getAppProvider());
                    operateLog.setAppConsumer(rule.getAppConsumer());
                    operateLog.setInterfacePath(rule.getInterfacePath());
                    operateLog.setOperateId(batchId);
                    operateLog.setBeforeValue(null);
                    operateLog.setErp(erp);
                    operateLog.setOperateType(OperateType.CREATE.type);
                    operateLog.setAfterValue(JSONObject.toJSONString(rule));
                    operateLog.setCreateTime(date);
                    logList.add(operateLog);
                }
                operateLogService.saveBatch(logList);
            }
        } catch (Exception e) {
            Profiler.businessAlarm("com.jd.workflow.console.service.ratelimiting.RateLimitingLogAspect", System.currentTimeMillis(), "服务总线限流日志写入异常，详情：" + Arrays.toString(joinPoint.getArgs()));
            log.info("服务总线限流日志写入异常，详情：" + Arrays.toString(joinPoint.getArgs()));
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 删除日志规则
     */
    @Pointcut("@annotation(com.jd.workflow.console.service.ratelimiting.annotation.DeleteLog)")
    public void deleteLog() {
    }

    @Around("deleteLog()")
    public Object deleteLogAround(ProceedingJoinPoint joinPoint) throws Throwable {
        List<RateLimitingRules> resultList = null;
        String erp = null;
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length >= 2) {
                Object arg = args[0];
                if (arg instanceof List) {
                    List<?> ids = (List<?>) arg;
                    List<Long> idsList = new ArrayList<>();
                    for (Object obj : ids) {
                        if (obj instanceof Long) {
                            idsList.add((Long) obj);
                        }
                    }
                    resultList = rateLimitingRulesService.listByIds(idsList);
                }
                Object arg1 = args[1];
                if (arg1 instanceof String) {
                    erp = (String) arg1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        Object result;
        try {
            Object[] args = joinPoint.getArgs();
            log.info("删除规则日志参数：" + Arrays.toString(args));
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw throwable;
        }

        //记录日志，捕获异常
        try {
            if (resultList!=null && resultList.size()>0){
                long batchId = System.currentTimeMillis();
                Date date = new Date();
                List<RateLimitingRulesOperateLog> logList = new ArrayList<>();
                for (RateLimitingRules rule : resultList) {
                    RateLimitingRulesOperateLog operateLog = new RateLimitingRulesOperateLog();
                    operateLog.setAppProvider(rule.getAppProvider());
                    operateLog.setAppConsumer(rule.getAppConsumer());
                    operateLog.setInterfacePath(rule.getInterfacePath());
                    operateLog.setOperateId(batchId);
                    operateLog.setBeforeValue(JSONObject.toJSONString(rule));
                    operateLog.setErp(erp);
                    operateLog.setOperateType(OperateType.DELETE.type);
                    operateLog.setAfterValue(null);
                    operateLog.setCreateTime(date);
                    logList.add(operateLog);
                }
                operateLogService.saveBatch(logList);
            }
        } catch (Exception e) {
            Profiler.businessAlarm("com.jd.workflow.console.service.ratelimiting.RateLimitingLogAspect", System.currentTimeMillis(), "服务总线限流日志写入异常，详情：" + Arrays.toString(joinPoint.getArgs()));
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 更新日志规则
     */
    @Pointcut("@annotation(com.jd.workflow.console.service.ratelimiting.annotation.UpdateLog)")
    public void updateLog() {
    }


    @Around("updateLog()")
    public Object updateLogAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Map<Long, RateLimitingRules> map = new HashMap<>();
        String erp = null;
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length >= 2) {
                Object arg = args[0];
                Object arg1 = args[1];
                if (arg instanceof List) {
                    List<?> updateList = (List<?>) arg;
                    for (Object temp : updateList) {
                        if (temp instanceof RateLimitingRules) {
                            RateLimitingRules rateLimitingRules = (RateLimitingRules) temp;
                            if (rateLimitingRules.getId() != null) {
                                map.put(rateLimitingRules.getId(), rateLimitingRulesService.getById(rateLimitingRules.getId()));
                            }
                        }
                    }
                }
                if (arg1 instanceof String) {
                    erp = (String) arg1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Object result;
        try {
            Object[] args = joinPoint.getArgs();
            log.info("添加规则日志参数：" + Arrays.toString(args));
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw throwable;
        }

        //记录日志，捕获异常
        try {
            long batchId = System.currentTimeMillis();
            List<RateLimitingRules> rateLimitingRules = rateLimitingRulesService.listByIds(map.keySet());
            Date date = new Date();
            for (RateLimitingRules rule : rateLimitingRules) {
                RateLimitingRulesOperateLog operateLog = new RateLimitingRulesOperateLog();
                operateLog.setAppProvider(rule.getAppProvider());
                operateLog.setAppConsumer(rule.getAppConsumer());
                operateLog.setInterfacePath(rule.getInterfacePath());
                operateLog.setOperateId(batchId);
                operateLog.setBeforeValue(JSONObject.toJSONString(map.getOrDefault(rule.getId(), null)));
                operateLog.setOperateType(OperateType.UPDATE.type);
                operateLog.setAfterValue(JSONObject.toJSONString(rule));
                operateLog.setCreateTime(date);
                operateLog.setErp(erp);
                operateLogService.save(operateLog);
            }
        } catch (Exception e) {
            Profiler.businessAlarm("com.jd.workflow.console.service.ratelimiting.RateLimitingLogAspect", System.currentTimeMillis(), "服务总线限流日志写入异常，详情：" + Arrays.toString(joinPoint.getArgs()));
            e.printStackTrace();
        }
        return result;
    }

    @Pointcut("@annotation(com.jd.workflow.console.service.ratelimiting.annotation.PublishLog)")
    public void publishLog() {
    }

    @Around("publishLog()")
    public Object publishLogAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        try {
            Object[] args = joinPoint.getArgs();
            log.info("添加规则日志参数：" + Arrays.toString(args));
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw throwable;
        }

        //记录日志，捕获异常
        try {
            Integer status = null;
            List<Long> idList = null;
            String erp = null;
            Object[] args = joinPoint.getArgs();
            if (args.length > 2) {
                Object arg = args[0];
                Object arg1 = args[1];
                if (arg instanceof RateLimitingChangeStatusDTO) {
                    RateLimitingChangeStatusDTO statusDTO = (RateLimitingChangeStatusDTO) arg;
                    idList = statusDTO.getIdList();
                    status = statusDTO.getStatus();
                }
                if (arg1 instanceof String) {
                    erp = (String) arg1;
                }
            }

            long batchId = System.currentTimeMillis();
            List<RateLimitingRules> rateLimitingRules = rateLimitingRulesService.listByIds(idList);
            Date date = new Date();
            for (RateLimitingRules rule : rateLimitingRules) {
                RateLimitingRulesOperateLog operateLog = new RateLimitingRulesOperateLog();
                operateLog.setAppProvider(rule.getAppProvider());
                operateLog.setAppConsumer(rule.getAppConsumer());
                operateLog.setInterfacePath(rule.getInterfacePath());
                operateLog.setOperateId(batchId);
                operateLog.setBeforeValue(null);
                operateLog.setAfterValue(null);
                if (status != null && status == 1) {
                    operateLog.setOperateType(OperateType.ONLINE.type);
                } else {
                    operateLog.setOperateType(OperateType.OFFLINE.type);
                }
                operateLog.setCreateTime(date);
                operateLog.setErp(erp);
                operateLogService.save(operateLog);
            }
        } catch (Exception e) {
            Profiler.businessAlarm("com.jd.workflow.console.service.ratelimiting.RateLimitingLogAspect", System.currentTimeMillis(), "服务总线限流日志写入异常，详情：" + Arrays.toString(joinPoint.getArgs()));
            e.printStackTrace();
        }
        return result;
    }

    @Pointcut("@annotation(com.jd.workflow.console.service.ratelimiting.annotation.AddGlobalLog)")
    public void addGlobalLog() {
    }

    @Around("addGlobalLog()")
    public Object addGlobalLogAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        Object[] args = joinPoint.getArgs();
        RateLimitingRulesConfig paramConfig = null;
        RateLimitingRulesConfig dbConfig = null;
        String erp = null;
        try {
            if (args.length == 2) {
                Object arg0 = args[0];
                Object arg1 = args[1];
                if (arg0 instanceof RateLimitingRulesConfig) {
                    paramConfig = (RateLimitingRulesConfig) arg0;
                    String appProvider = paramConfig.getAppProvider();
                    if (StringUtils.isNotBlank(appProvider)) {
                        LambdaQueryWrapper<RateLimitingRulesConfig> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                        lambdaQueryWrapper.eq(RateLimitingRulesConfig::getAppProvider, appProvider);
                        dbConfig = rateLimitingRulesConfigService.getOne(lambdaQueryWrapper);
                    }
                }
                if (arg1 instanceof String) {
                    erp = (String) arg1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //执行逻辑
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw throwable;
        }

        //记录日志
        try {
            if (paramConfig != null) {
                String appProvider = paramConfig.getAppProvider();
                LambdaQueryWrapper<RateLimitingRulesConfig> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(RateLimitingRulesConfig::getAppProvider, appProvider);
                RateLimitingRulesConfig finalConfig = rateLimitingRulesConfigService.getOne(lambdaQueryWrapper);
                long batchId = System.currentTimeMillis();
                RateLimitingRulesOperateLog operateLog = new RateLimitingRulesOperateLog();
                operateLog.setAppProvider(appProvider);
                operateLog.setOperateId(batchId);
                operateLog.setCreateTime(new Date());
                operateLog.setErp(erp);

                if (dbConfig == null) {//新增
                    operateLog.setInterfacePath("新增全局配置");
                    operateLog.setOperateType(OperateType.CREATE.type);
                    operateLog.setBeforeValue(null);
                    operateLog.setAfterValue(JSONObject.toJSONString(finalConfig));
                } else {//修改
                    operateLog.setInterfacePath("修改全局配置");
                    operateLog.setOperateType(OperateType.UPDATE.type);
                    operateLog.setBeforeValue(JSONObject.toJSONString(dbConfig));
                    operateLog.setAfterValue(JSONObject.toJSONString(finalConfig));
                }
                operateLogService.save(operateLog);
            }
        } catch (Exception e) {
            Profiler.businessAlarm("com.jd.workflow.console.service.ratelimiting.RateLimitingLogAspect", System.currentTimeMillis(), "服务总线限流日志写入异常，详情：" + Arrays.toString(joinPoint.getArgs()));
            e.printStackTrace();
        }
        return result;
    }
}