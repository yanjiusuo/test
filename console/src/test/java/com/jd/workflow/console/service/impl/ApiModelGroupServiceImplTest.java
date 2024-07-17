package com.jd.workflow.console.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/26
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiModelGroupServiceImplTest {


    @Autowired
    private ApiModelGroupServiceImpl apiModelGroupService;

    @Test
    public void addGroup() {
        apiModelGroupService.addGroup(1L, "test9", "test9", null);

    }

    @Test
    public void modifyGroupName() {
    }

    @Test
    public void removeGroup() {
    }

    @Test
    public void findMethodGroupTree() {
    }

    @Test
    public void modifyMethodGroupTree() {
    }
}