package com.jd.workflow;

import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

@RunWith(JUnit4.class)
@Slf4j
public class BaseTestCase  extends Assert {
    public static final Logger logger = LoggerFactory.getLogger(BaseTestCase.class);
    protected File loadFile(String path){

        try {
            return ResourceUtils.getFile(path);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
    @Before
    public void setUp() throws Exception {

    }
    @After
    public void tearDown() throws Exception {

    }
    protected String getResourceContent(String path){

        try {
            File file = ResourceUtils.getFile(path);
            return IOUtils.toString(new FileInputStream(file),"utf-8");
        } catch (Exception e) {
            return null;
        }
    }

    private static String formatArgument(String argument) {
        String argumentWithoutWhiteSpaces = argument.trim();
        return argumentWithoutWhiteSpaces;
    }

    private static String constructResourcePath(String resourcePath) {
        //String resourcePath = String.format("/%s/%s", resourcePath, resourceName);
        String resourcePathUnixSeparators = FilenameUtils.separatorsToUnix(resourcePath);
        String resourcePathNoLeadingSeparators = removeLeadingUnixSeparators(resourcePathUnixSeparators);
        String normalizedResourcePath = FilenameUtils.normalizeNoEndSeparator(resourcePathNoLeadingSeparators, true);
        return normalizedResourcePath;
    }
    private static String removeLeadingUnixSeparators(String argument) {
        return argument.replaceAll("/+", "/");
    }

    public static void assertJsonEquals(String json1,String json2){
        if(StringUtils.isBlank(json1) && StringUtils.isBlank(json2)){
            return;
        }
        assertJsonObjEquals(JsonUtils.parse(json1),JsonUtils.parse(json2));
    }
    private static void assertJsonObjEquals(Object obj1,Object obj2){
        if(obj1 instanceof Map){
            if(!(obj2 instanceof Map)){
                throw new AssertionError("类型不匹配");
            }
            Map map1 = (Map) obj1;
            Map map2 = (Map) obj2;
            if(map1.size() != map2.size()) {
                throw new AssertionError("map size不匹配");
            }
            for (Object o : map1.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                final Object val2 = map2.get(entry.getKey());

                assertJsonObjEquals(entry.getValue(),val2);

            }
        }else if(obj1 instanceof List){
            List list1 = (List) obj1;
            List list2 = (List) obj2;
            if(list1.size() != list2.size()){
                throw new AssertionError("list size不匹配");
            }
            for (int i = 0; i < list1.size(); i++) {
                assertJsonObjEquals(list1.get(i),list2.get(i));
            }
        }else{
            if(!ObjectHelper.equals(obj1,obj2)){
                throw new AssertionError("not equals value value1=" +obj1+" value2 ="+ obj2);
            }
        }
    }

}
