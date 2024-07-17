package com.jd.workflow.console.service.impl;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.EasyWebService;
import com.jd.workflow.console.dto.EnvModel;
import com.jd.workflow.console.entity.MethodManage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.*;


public class MethodManageServiceImplTests extends BaseTestCase {
    String webserviceUrl = "http://localhost:3001/test";
    String wsdlUrl = webserviceUrl+"?wsdl";

    MethodManageServiceImpl methodManageService;
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        EasyWebService.run(webserviceUrl);
        methodManageService = Mockito.spy(new MethodManageServiceImpl());

    }

    String weatherWsdl = "http://www.webxml.com.cn/WebServices/WeatherWebService.asmx?wsdl";
    MethodManageServiceImpl service = new MethodManageServiceImpl();

    @Test
    public void testParse() throws Exception {
        List<EnvModel> envModels = new ArrayList<>();
        List<MethodManage> methodManages = service.wsdlToMethod(123L, weatherWsdl,envModels);
        assertEquals(1,envModels.size());
        System.out.println(methodManages);
    }
    MethodManage newMethod(Long id,String name,String content){
        MethodManage manage = new MethodManage();
        manage.setId(id);
        manage.setName(name);
        manage.setContent(content);
        return manage;
    }
    @Test
    public void testMergeMethods(){
        List<MethodManage> oldMethods = new ArrayList<>();
        oldMethods.add(newMethod(null,"addMethod1","content"));
        oldMethods.add(newMethod(null,"updateMethod","content"));


        List<MethodManage> newMethods = new ArrayList<>();
        newMethods.add(newMethod(1L,"updateMethod","content"));
        newMethods.add(newMethod(2L,"removeMethod","content"));

        Mockito.doReturn(newMethods).when(methodManageService).getInterfaceMethods(anyLong());
        Mockito.doAnswer(vs->{
            List<MethodManage> added = vs.getArgumentAt(0,List.class);
            assertEquals(1,added.size());
            assertEquals(oldMethods.get(0),added.get(0));
            return null;
        }).when(methodManageService).saveBatch(Mockito.anyList());
        Mockito.doAnswer(vs->{
            MethodManage updated = vs.getArgumentAt(0,MethodManage.class);

            assertEquals(oldMethods.get(1),updated);
            return null;
        }).when(methodManageService).updateById(any());
      /*  Mockito.doAnswer((vs)->{
            MethodManageServiceImpl methodManageService = new MethodManageServiceImpl();

            methodManageService.mergeMethods((List)vs.getArguments()[0], (Long) vs.getArguments()[1]);
            return null;
        }).when(methodManageService).mergeMethods(anyList(),anyLong());
*/
        Mockito.doAnswer(vs->{
            List<Long> removed = vs.getArgumentAt(0,List.class);
            assertEquals(2L,(long)removed.get(0));
            return null;
        }).when(methodManageService).removeByIds(Mockito.anyList());
        methodManageService.mergeMethods(oldMethods,1L);
    }
}
