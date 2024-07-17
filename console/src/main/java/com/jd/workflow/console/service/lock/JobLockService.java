package com.jd.workflow.console.service.lock;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.IpUtil;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.LockTypeEnum;
import com.jd.workflow.console.dao.mapper.lock.JobLockMapper;
import com.jd.workflow.console.entity.JobLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 项目名称：parent
 * 类 名 称：JobLockService
 * 类 描 述：TODO
 * 创建时间：2022-12-16 15:16
 * 创 建 人：wangxiaofei8
 */
@Slf4j
@Component
public class JobLockService extends ServiceImpl<JobLockMapper, JobLock> {

    public boolean createLock(LockTypeEnum lockType , String lockValue){
        try {
            log.info("JobLockService.begin_create_lock:type={},value={}",lockType,lockValue);
            JobLock entity = new JobLock();
            entity.setType(lockType.getCode());
            entity.setLockValue(lockValue);
            entity.setIp(IpUtil.getLocalIp());
            entity.setCreated(new Date());
            entity.setCreator(UserSessionLocal.getUser() == null ? null :UserSessionLocal.getUser().getUserId());
            this.save(entity);
            return true;
        } catch (Exception e) {
//            log.error("JobLockService.create_lock occur exception , msg={}",e.getMessage(),e);
            return false;
        }
    }


    public boolean getLock(LockTypeEnum lockType ,String lockValue){
        try {
            JobLock lockObj = this.getOne(Wrappers.<JobLock>lambdaQuery().eq(JobLock::getType, lockType.getCode()).eq(JobLock::getLockValue, lockValue));
            log.info("JobLockService.get_lock:lockType={},lockValue={}",lockType,lockValue);
            if(lockObj!=null&&IpUtil.getLocalIp().equals(lockObj.getIp())){
                return true;
            }
        } catch (Exception e) {
//            log.error("JobLockService.getLock occur exception , msg={}",e.getMessage(),e);
        }
        return false;
    }

}
