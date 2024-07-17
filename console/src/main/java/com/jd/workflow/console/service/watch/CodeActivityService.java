package com.jd.workflow.console.service.watch;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dao.mapper.watch.CodeActivityMapper;
import com.jd.workflow.console.entity.watch.CodeActivity;
import com.jd.workflow.console.entity.watch.dto.CodeActivityDto;
import com.jd.workflow.console.entity.watch.dto.CodeActivityTypeEnum;
import com.jd.workflow.console.service.doc.SwaggerParserService;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StdCalendar;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CodeActivityService extends ServiceImpl<CodeActivityMapper, CodeActivity> {

    ExecutorService defaultScheduledExecutor;

    @PostConstruct
    public void init() {
        defaultScheduledExecutor = new ScheduledThreadPoolExecutor(1);
    }

    public List<CodeActivity> fetchDayCodeActivities(Date currentDay, CodeActivityTypeEnum type, long current, Long pageSize) {

        LambdaQueryWrapper<CodeActivity> lqw = new LambdaQueryWrapper<>();

        Date startDay = StdCalendar.fromString(StringHelper.formatDate(currentDay, "yyyy-MM-dd")).getTime();
        Date endDay = StdCalendar.fromString(StringHelper.formatDate(currentDay, "yyyy-MM-dd")).moveDay(1).getTime();


        lqw.ge(CodeActivity::getTime, startDay);
        lqw.le(CodeActivity::getTime, endDay);
        lqw.select(CodeActivity::getId, CodeActivity::getType, CodeActivity::getTime, CodeActivity::getErp, CodeActivity::getCostTime);
        lqw.eq(CodeActivity::getType, type.name());
        lqw.orderByAsc(CodeActivity::getTime);
        IPage<CodeActivity> page = new Page<>(current, pageSize);
        IPage<CodeActivity> codeActivities = page(page, lqw);
        return codeActivities.getRecords();
    }

    public CodeActivity fetchLastCodeActivity(CodeActivity activity) {
        LambdaQueryWrapper<CodeActivity> lqw = new LambdaQueryWrapper<>();
        lqw.lt(CodeActivity::getTime, activity.getTime());
        lqw.select(CodeActivity::getId, CodeActivity::getTime, CodeActivity::getErp, CodeActivity::getCostTime);
        lqw.eq(CodeActivity::getType, activity.getType());
        lqw.eq(CodeActivity::getErp, activity.getErp());
        lqw.orderByDesc(CodeActivity::getTime);
        lqw.last("limit 1");
        return getOne(lqw);
    }

    public CodeActivity fetchLastCodeActivity(CodeActivityTypeEnum type) {
        LambdaQueryWrapper<CodeActivity> lqw = new LambdaQueryWrapper<>();
        lqw.orderByDesc(CodeActivity::getTime);
        lqw.select(CodeActivity::getId, CodeActivity::getTime, CodeActivity::getErp, CodeActivity::getCostTime);
        lqw.eq(CodeActivity::getType, type.name());
        lqw.last("limit 1");
        return getOne(lqw);
    }

    public void saveActivities(List<CodeActivityDto> dtos) {
        defaultScheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    asyncSaveActivities(dtos);
                } catch (Exception e) {
                    log.error("codeActivity.err_sendActivities", e);
                }
            }
        });
    }

    public List<CodeActivityDto> limitFileSaveEvent(List<CodeActivityDto> dtos) {
        Map<String, Long> filePath2Time = new HashMap<>();
        List<CodeActivityDto> result = new ArrayList<>();
        for (CodeActivityDto dto : dtos) {
            if ("fileSave".equals(dto.getEventType())) {
                Long lastTime = filePath2Time.computeIfAbsent(dto.getFilePath(), k -> 0L);
                if (dto.getTime() - lastTime >= 1000 * 60 * 2) {
                    filePath2Time.put(dto.getFilePath(), dto.getTime());
                    result.add(dto);
                }
                continue;
            }
            result.add(dto);

        }
        return result;
    }

    private void asyncSaveActivities(List<CodeActivityDto> dtos) {
        dtos = limitFileSaveEvent(dtos);
        if (dtos.isEmpty()) return;
        CodeActivityDto firstDto = dtos.get(0);
        String erp = firstDto.getErp();
        if (StringUtils.isEmpty(erp)) {
            erp = firstDto.getUserName();
        }
        UserInfoInSession session = new UserInfoInSession();
        session.setUserName(erp);
        UserSessionLocal.setUser(session);
        try{
            List<CodeActivity> activities = dtos.stream().map(d -> {
                CodeActivity c = new CodeActivity();
                c.setTime(new Timestamp(d.getTime()));
                c.setErp(StringUtils.isEmpty(d.getErp()) ? d.getUserName() : d.getErp());
                c.setUserName(d.getUserName());
                c.setProject(d.getProject());
                c.setCodeRepository(d.getCodeRepository());
                c.setBranch(d.getBranch());
                c.setEventType(d.getEventType());
                c.setSubType(d.getSubType());
                c.setLanguage(d.getLanguage());
                c.setType(d.getType());
                c.setFilePath(d.getFilePath());
                c.setIsWrite(d.isWrite() ? 1 : 0);
                c.setLineNumber(d.getLineNumber());
                c.setLineCount(d.getLineCount());
                c.setCursorPosition(d.getCursorPosition());
                c.setCostTime(d.getCostTime());
                c.setBuildType(d.getBuildType());
                c.setBuildSuccess(d.isBuildSuccess() ? 1 : 0);
                c.setBuildFileCount(d.getBuildFileCount());

                c.setChannel(d.getChannel());
                // 最大长度4096
                c.setFullGitInfo(SwaggerParserService.truncateStr(d.getFullGitInfo(),4096));

                return c;
            }).collect(Collectors.toList());
            saveAllActivities(activities);
        }finally {
            UserSessionLocal.removeUser();
        }

    }

    public void saveAllActivities(List<CodeActivity> activities) {
        if (activities.isEmpty()) return;
        Collections.sort(activities, new Comparator<CodeActivity>() {
            @Override
            public int compare(CodeActivity o1, CodeActivity o2) {
                return o1.getTime().compareTo(o2.getTime());
            }
        });
        if (activities.size() < 2) {
            saveBatch(activities);
            return;
        }
        LambdaQueryWrapper<CodeActivity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CodeActivity::getErp, activities.get(0).getErp());
        lqw.ge(CodeActivity::getTime, activities.get(0).getTime());
        lqw.le(CodeActivity::getTime, activities.get(activities.size() - 1).getTime());
        List<CodeActivity> list = list(lqw);
        Map<Long, List<CodeActivity>> time2Activity = list.stream().collect(Collectors.groupingBy(vs -> {
            return vs.getTime().getTime();
        }));
        List<CodeActivity> canSaved = activities.stream().filter(item -> {
            List<CodeActivity> vs = time2Activity.get(item.getTime().getTime());
            if (vs == null || vs.isEmpty()) {
                return true;
            }
            return vs.stream().noneMatch(v -> {
                return ObjectHelper.equals(v.getEventType(), item.getEventType()) && ObjectHelper.equals(v.getFilePath(), item.getFilePath());
            });
        }).collect(Collectors.toList());
        if (!canSaved.isEmpty()) {
            saveBatch(canSaved);
        }


    }

    public List<CodeActivity> fetchErpCodeActivity(String erp, long current, long size, Timestamp lastId) {
        LambdaQueryWrapper<CodeActivity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CodeActivity::getErp, erp);
        lqw.orderByAsc(CodeActivity::getTime);
        lqw.gt(lastId != null, CodeActivity::getTime, lastId);
        Page<CodeActivity> page = (Page<CodeActivity>) page(new Page<>(current, size), lqw);
        return page.getRecords();
    }
    @Transactional(propagation= Propagation.NEVER)
    public void clearOneMonthCodeData(){
        LambdaQueryWrapper<CodeActivity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CodeActivity::getType, CodeActivityTypeEnum.heartbeat.name());
        lqw.select(CodeActivity::getId);
        lqw.last(" limit 500");
        lqw.orderByAsc(CodeActivity::getId);
        //lqw.le(CodeActivity::getTime, new StdCalendar().moveDay(-14).getTime());
        int start = 1;
        while(true){

            List<CodeActivity> activities = list(lqw);
            if(activities.isEmpty()){
                break;
            }
            log.info("code.alread_deleted:{}"+(start*100));
            List<Long> ids = activities.stream().map(item -> item.getId()).collect(Collectors.toList());
            removeByIds(ids);
        }
    }

    public void removeErpActivity(String erp) {
        LambdaQueryWrapper<CodeActivity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CodeActivity::getErp, erp);
        remove(lqw);
    }
    public void compactCodeActivity(String erp) {
        long current = 1;
        long size = 1000;
        Timestamp lastId = null;
        while (true) {
            try {
                List<CodeActivity> codeActivities = fetchErpCodeActivity(erp, current, size, lastId);
                if (codeActivities == null || codeActivities.isEmpty()) break;
                lastId = codeActivities.get(codeActivities.size() - 1).getTime();
                //current++;
                List<Long> ids = getLimitRemovedIds(codeActivities);

                if (!ids.isEmpty()) {
                    log.info("compactCodeActivity erp:{},current={} size:{}", erp, current,ids.size());
                    removeByIds(ids);
                }
            } catch (Exception e) {
                log.error("compactCodeActivity error", e);
            }
        }
    }

    public List<Long> getLimitRemovedIds(List<CodeActivity> dtos) {
        Map<String, Long> filePath2Time = new HashMap<>();
        List<Long> result = new ArrayList<>();
        for (CodeActivity dto : dtos) {
            if ("fileSave".equals(dto.getEventType())) {
                Long lastTime = filePath2Time.computeIfAbsent(dto.getFilePath(), k -> 0L);
                if (dto.getTime().getTime() - lastTime < 1000 * 60 * 2) {
                    result.add(dto.getId());
                } else {
                    filePath2Time.put(dto.getFilePath(), dto.getTime().getTime());

                }
                continue;
            }


        }
        return result;
    }

    public IPage<CodeActivity> pageList(long current, long size, String type) {
        LambdaQueryWrapper<CodeActivity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CodeActivity::getType, type);
        Page<CodeActivity> page = page(new Page<>(current, size), lqw);
        return page;
    }
}
