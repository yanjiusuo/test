package com.jd.workflow.soap;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;

@RunWith(JUnit4.class)
public class BaseTestCase extends Assert {
    protected String getResourceContent(String path){

        try {
            File file = ResourceUtils.getFile(path);
            return IOUtils.toString(new FileInputStream(file),"utf-8");
        } catch (Exception e) {
            return null;
        }
    }
}
