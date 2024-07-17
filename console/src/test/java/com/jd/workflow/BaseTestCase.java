package com.jd.workflow;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
@RunWith(JUnit4.class)
public class BaseTestCase extends TestCase {
    protected File loadFile(String path){

        try {
            return ResourceUtils.getFile(path);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
    protected String getResourceContent(String path){

        try {
            File file = ResourceUtils.getFile(path);
            return IOUtils.toString(new FileInputStream(file),"utf-8");
        } catch (Exception e) {
            return null;
        }
    }
}
