package com.jd.workflow.console.service.requirement;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alibaba.fastjson.JSON;
import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.ConsoleApplication;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dto.requirement.*;
import com.jd.workflow.console.entity.requirement.RequirementInfoLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/1
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {ConsoleApplication.class},                                          // spring boot 的启动类
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT      // 启动容器使用随机端口号, 可以不用
)
@Slf4j
public class InterfaceSpaceServiceTest extends BaseTestCase {


    @Autowired
    private InterfaceSpaceService interfaceSpaceService;
    @Autowired
    private RequirementInfoLogService requirementInfoLogService;

    @Test
    public void testCreateSpace() {
        initLoginUser();
        // Create a new InterfaceSpaceDTO object
        InterfaceSpaceDTO interfaceSpaceDTO = new InterfaceSpaceDTO();
        interfaceSpaceDTO.setName("接口空间1");
        interfaceSpaceDTO.setDesc("111");
        interfaceSpaceDTO.setOwner("wangjingfang3");
        List<String> userList = Lists.newArrayList();
        userList.add("sunchao81");
        interfaceSpaceDTO.setMembers(userList);

        // Set the necessary properties of interfaceSpaceDTO

        // Call createSpace method and assert the returned value
        Long result = interfaceSpaceService.createSpace(interfaceSpaceDTO);
        assertNotNull(result);
        System.out.println("result:" + result);
    }

    @Test
    public void testEditSpace() {
        // Create an existing InterfaceSpaceDTO object
        initLoginUser();
        // Create a new InterfaceSpaceDTO object
        InterfaceSpaceDTO interfaceSpaceDTO = new InterfaceSpaceDTO();
        interfaceSpaceDTO.setId(2347L);
        interfaceSpaceDTO.setName("新建测试空间11");
        interfaceSpaceDTO.setDesc("测试编辑这是我新建的空间呀");
//        interfaceSpaceDTO.setOwner("chenyufeng18");
//        List<String> userList = Lists.newArrayList();
//        userList.add("wangjingfang3");
//        interfaceSpaceDTO.setMembers(userList);

        // Set the necessary properties of interfaceSpaceDTO

        // Call editSpace method and assert the returned value
        Long result = interfaceSpaceService.editSpace(interfaceSpaceDTO);
        assertNotNull(result);
    }

    @Test
    public void testDeleteSpace() {
        // Create an existing InterfaceSpaceDTO object
        initLoginUser();
        InterfaceSpaceDTO interfaceSpaceDTO = new InterfaceSpaceDTO();
        interfaceSpaceDTO.setId(634L);
        // Set the necessary properties of interfaceSpaceDTO

        // Call deleteSpace method and assert the returned value
        Long result = interfaceSpaceService.deleteSpace(interfaceSpaceDTO);
        assertNotNull(result);
    }

    @Test
    public void testGetSpaceInfo() {
        initLoginUser();
        // Provide a valid spaceId
        Long spaceId = 2344L;

        // Call getSpaceInfo method and assert the returned value
        InterfaceSpaceDetailDTO result = interfaceSpaceService.getSpaceInfo(spaceId);
        assertNotNull(result);
        System.out.println("result:" + JSON.toJSONString(result));
    }

    @Test
    public void testGetSpaceInfoStatic() {
        initLoginUser();

        // Provide a valid spaceId  164L
        Long spaceId = 2344L;

        // Call getSpaceInfoStatic method and assert the returned value
        InterfaceSpaceStaticDTO result = interfaceSpaceService.getSpaceInfoStatic(spaceId);
        assertNotNull(result);
        System.out.println("result:" + JSON.toJSONString(result));
    }

    @Test
    public void testGetSpaceUser() {
        // Provide a valid spaceId
        Long spaceId = 2344L;

        // Call getSpaceUser method and assert the returned value
        InterfaceSpaceUser result = interfaceSpaceService.getSpaceUser(spaceId);
        assertNotNull(result);
        System.out.println("result:" + JSON.toJSONString(result));
    }

    @Test
    public void testRemoveUser() {
        initLoginUser();

        // Create a RemoveSpaceUserDTO object
        RemoveSpaceUserDTO removeSpaceUser = new RemoveSpaceUserDTO();
        removeSpaceUser.setSpaceId(2344L);
        List<String> userList = Lists.newArrayList();

        userList.add("wangjingfang3");
        removeSpaceUser.setUserErpList(userList);

        // Set the necessary properties of removeSpaceUser

        // Call removeUser method and assert the returned value
        Boolean result = interfaceSpaceService.removeUser(removeSpaceUser);
        assertTrue(result);
    }

    @Test
    public void testAddUser() {
        // Create an AddSpaceUserDTO object
        AddSpaceUserDTO addSpaceUserDTO = new AddSpaceUserDTO();
        addSpaceUserDTO.setSpaceId(2344L);

        initLoginUser();

        List<String> userList = Lists.newArrayList();

        userList.add("wangjingfang3");
        userList.add("zhangqian346");

        addSpaceUserDTO.setUserErpList(userList);

        // Set the necessary properties of addSpaceUserDTO

        // Call addUser method and assert the returned value
        Boolean result = interfaceSpaceService.addUser(addSpaceUserDTO);
        assertTrue(result);
        System.out.println("result:" + JSON.toJSONString(result));
    }

    private void initLoginUser() {
        UserInfoInSession user = new UserInfoInSession();
        user.setUserId("wangjingfang3");
        UserSessionLocal.setUser(user);
    }

    @Test
    public void testCheckUser() {
        // Provide a valid spaceId and erp
        Long spaceId = 2344L;
        String erp = "chenyufeng18";

        // Call checkUser method and assert the returned value
        InterfaceSpaceUser result = interfaceSpaceService.checkUser(spaceId, erp);

        System.out.println("result:" + JSON.toJSONString(result));
    }

    @Test
    public void testQuerySpaceList() {
        initLoginUser();

        // Create an InterfaceSpaceParam object
        InterfaceSpaceParam interfaceSpaceParam = new InterfaceSpaceParam();
        interfaceSpaceParam.setCurrent(1L);
        interfaceSpaceParam.setSize(20L);

        // Set the necessary properties of interfaceSpaceParam

        // Call querySpaceList method and assert the returned value
        Page<InterfaceSpaceDetailDTO> result = interfaceSpaceService.querySpaceList(interfaceSpaceParam);
        System.out.println("result:" + JSON.toJSONString(result));
        assertNotNull(result);
    }

    @Test
    public void getLog() {
        initLoginUser();
        InterfaceSpaceLogParam interfaceSpaceLogParam = new InterfaceSpaceLogParam();
        interfaceSpaceLogParam.setSpaceId(2344L);
        interfaceSpaceLogParam.setCurrent(1L);
        interfaceSpaceLogParam.setSize(5L);
        Page<RequirementInfoLog> result = requirementInfoLogService.queryLog(interfaceSpaceLogParam);
        System.out.println("result:" + JSON.toJSONString(result));
    }
}