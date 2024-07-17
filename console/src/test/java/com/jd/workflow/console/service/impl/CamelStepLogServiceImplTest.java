package com.jd.workflow.console.service.impl;


import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.workflow.console.dto.CamelLogQueryDTO;
import com.jd.workflow.console.entity.CamelStepLog;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.PublishManage;
import com.jd.workflow.console.helper.ProjectHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.InjectMocks;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.IPublishManageService;
import com.jd.workflow.console.dto.CamelLogReqDTO;
import com.jd.workflow.console.dto.CamelLogConditionDTO;
import com.jd.workflow.console.dto.CamelLogListDTO;
import org.powermock.api.mockito.mockpolicies.Slf4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@MockPolicy(Slf4jMockPolicy.class)
@PowerMockIgnore({"jdk.internal.reflect.*"})
public class CamelStepLogServiceImplTest {

    @Before
    public void init(){
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), InterfaceManage.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), MethodManage.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), PublishManage.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), CamelStepLog.class);
    }

    @InjectMocks
    private CamelStepLogServiceImpl camelStepLogServiceImpl;

    @Mock
    private IInterfaceManageService interfaceManageService;

    @Mock
    private IMethodManageService methodManageService;

    @Mock
    private IPublishManageService publishManageService;

    @Mock
    private  BaseMapper baseMapper;

    @Mock
    private ProjectHelper projectHelper;





    @Test
    public void when_queryCamleStepLog_then_return_success(){
        List<CamelStepLog> list = new ArrayList<>();
        CamelStepLog obj = new CamelStepLog();
        obj.setId(1l);
        obj.setBusinessId("2");
        obj.setMethodId("2");
        obj.setVersion("1");
        obj.setLogContent("logContent>>>>>");
        obj.setLogLevel(1);
        obj.setCreated(new Date());
        list.add(obj);
        Page<CamelStepLog> pageResult = new Page<>();
        pageResult.setTotal(1l);
        pageResult.setRecords(list);
        when(baseMapper.selectPage(Mockito.any(),Mockito.any())).thenReturn(pageResult);
        when(methodManageService.getBaseMapper()).thenReturn(mapper);
        when(interfaceManageService.getBaseMapper()).thenReturn(mapper);
        when(projectHelper.getPublishUrl(Mockito.any())).thenReturn("http://publish_address/2");
        CamelLogReqDTO query = new CamelLogReqDTO();
        query.setMethodId(2l);
        query.setInterfaceId(3l);
        query.setLogLevel(1);
        query.setVersion("1");
        query.setStartDate(null);
        query.setEndDate(null);
        query.setCurrentPage(1);
        query.setPageSize(10);
        CamelLogListDTO returnResult = camelStepLogServiceImpl.queryCamleStepLog(query);
        assert returnResult != null;
    }

    @Test
    public void when_queryLogInterfaceCondition_then_return_success(){
        List<InterfaceManage> list = new ArrayList<>();
        InterfaceManage obj = new InterfaceManage();
        obj.setId(1l);
        obj.setName("testMethod12");
        obj.setCreated(new Date());
        list.add(obj);
        Page<InterfaceManage> pageResult = new Page<>();
        pageResult.setTotal(1l);
        pageResult.setRecords(list);
        when(interfaceManageService.page(Mockito.any(),Mockito.any())).thenReturn(pageResult);
        CamelLogReqDTO query = new CamelLogReqDTO();
        query.setName("testMethod");
        CamelLogConditionDTO returnResult = camelStepLogServiceImpl.queryLogInterfaceCondition(query);
        assert returnResult != null;
    }

    @Test
    public void when_queryLogMethodCondition_then_return_success(){
        List<MethodManage> list = new ArrayList<>();
        MethodManage obj = new MethodManage();
        obj.setId(2l);
        obj.setInterfaceId(3l);
        obj.setName("测试方法");
        list.add(obj);
        when(methodManageService.list(Mockito.any())).thenReturn(list);
        CamelLogQueryDTO query = new CamelLogQueryDTO();
        query.setInterfaceId(3l);
        CamelLogConditionDTO returnResult = camelStepLogServiceImpl.queryLogMethodCondition(query);
        assert returnResult != null;
    }

    @Test
    public void when_queryLogVersionCondition_then_return_success(){
        List<PublishManage> list = new ArrayList<>();
        PublishManage obj = new PublishManage();
        obj.setId(1l);
        obj.setRelatedMethodId(2l);
        obj.setVersionId(1);
        obj.setCreated(new Date());
        list.add(obj);
        Page<PublishManage> pageResult = new Page<>();
        pageResult.setTotal(1l);
        pageResult.setRecords(list);
        when(publishManageService.page(Mockito.any(),Mockito.any())).thenReturn(pageResult);
        CamelLogReqDTO query = new CamelLogReqDTO();
        query.setMethodId(2l);
        CamelLogConditionDTO returnResult = camelStepLogServiceImpl.queryLogVersionCondition(query);
        assert returnResult != null;
    }


    public static BaseMapper mapper =   new BaseMapper(){

        @Override
        public int insert(Object entity) {
            return 0;
        }

        @Override
        public int deleteById(Serializable id) {
            return 0;
        }

        @Override
        public int delete(Wrapper queryWrapper) {
            return 0;
        }

        @Override
        public int updateById(Object entity) {
            return 0;
        }

        @Override
        public int update(Object entity, Wrapper updateWrapper) {
            return 0;
        }

        @Override
        public Object selectById(Serializable id) {
            return null;
        }

        @Override
        public Object selectOne(Wrapper queryWrapper) {
            return null;
        }

        @Override
        public Integer selectCount(Wrapper queryWrapper) {
            return null;
        }

        @Override
        public List selectList(Wrapper queryWrapper) {
            if(MethodManage.class.equals(((LambdaQueryWrapper)queryWrapper).getEntityClass())){
                List<MethodManage> list = new ArrayList<>();
                MethodManage obj = new MethodManage();
                obj.setId(2l);
                obj.setInterfaceId(3l);
                obj.setName("测试方法");
                list.add(obj);
                return list;
            }
            List<InterfaceManage> list = new ArrayList<>();
            InterfaceManage obj = new InterfaceManage();
            obj.setId(3l);
            obj.setName("接口名称");
            list.add(obj);
            return list;
        }

        @Override
        public List<Map<String, Object>> selectMaps(Wrapper queryWrapper) {
            return null;
        }

        @Override
        public List<Object> selectObjs(Wrapper queryWrapper) {
            return null;
        }

        @Override
        public IPage<Map<String, Object>> selectMapsPage(IPage page, Wrapper queryWrapper) {
            return null;
        }

        @Override
        public IPage selectPage(IPage page, Wrapper queryWrapper) {
            return null;
        }

        @Override
        public List selectByMap(Map columnMap) {
            return null;
        }

        @Override
        public List selectBatchIds(Collection idList) {
            return null;
        }

        @Override
        public int deleteBatchIds(Collection idList) {
            return 0;
        }

        @Override
        public int deleteByMap(Map columnMap) {
            return 0;
        }
    };
}
