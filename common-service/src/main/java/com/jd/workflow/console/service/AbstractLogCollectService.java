package com.jd.workflow.console.service;

import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
@Slf4j
public class AbstractLogCollectService {


    protected boolean enabled=true;


    protected  Integer saveInterval = 5*1000;

    protected LogCollector logCollector;



    private BlockingQueue<LogEntity> queue = new LinkedBlockingDeque(10000);


    public void start(){
        if(enabled){
            new Thread(new LogConsumerTask(),"log_consumer_task").start();
        }
    }
    public void addLog(LogEntity logEntity){
        queue.offer(logEntity);
    }
    public void addLog(boolean isError,String methodId,Object msg,String pubVersion){
        LogEntity entity = new LogEntity();
        entity.setError(isError);
        entity.setBusinessName(methodId);
        entity.setMethodName(methodId);
        entity.setMsg(JsonUtils.toJSONString(msg));
        entity.setPublishVersion(pubVersion);
        queue.offer(entity);
    }

    public class LogConsumerTask implements Runnable{

        @Override
        public void run() {
            while (true){
                sleep(AbstractLogCollectService.this.saveInterval);
                try{
                    List<LogEntity> list = new ArrayList();
                    queue.drainTo(list);
                    if(!list.isEmpty()){

                        log.info("step.save_log:size={}",list.size());
                        logCollector.collect(list);
                    }
                }catch (Exception e){
                    log.error("route.err_update_route",e);
                }

            }
        }
        protected void sleep(long timeout) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException var4) {
                Thread.currentThread().interrupt();
            }

        }
    }
}
