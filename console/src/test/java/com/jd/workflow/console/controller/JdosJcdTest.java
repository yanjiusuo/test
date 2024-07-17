package com.jd.workflow.console.controller;

import cn.hutool.json.JSONUtil;
import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.dto.plugin.jdos.JdosSystemApps;
import com.jd.workflow.console.service.plugin.jdos.JdosJcd;
import org.junit.Test;

import java.util.List;

public class JdosJcdTest extends BaseTestCase {

    JdosJcd jdosJcd = new JdosJcd();

    @Test
    public void testGetSystemApps() {
        List<JdosSystemApps> sunchao81 = jdosJcd.getSystemApps("sunchao81");
        System.out.println(JSONUtil.toJsonStr(sunchao81));
    }

    @Test
    public void testGetGroups() {
    }

    @Test
    public void testGetIps() {
    }
}