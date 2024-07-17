package com.jd.workflow.console.utils;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.BeanTool;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.xml.schema.ComplexJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.jd.workflow.console.utils.DeltaHelper.*;

@RunWith(JUnit4.class)
public class DeltaHelperTests extends BaseTestCase {
    public static List<String> needDeltaAttrs = Arrays.asList("desc","required","value","name","type","extAttrs");

    public void deltaTest(String contentPath){
        String  resource = getResourceContent("classpath:" + contentPath);
        List content = JsonUtils.parse(resource, List.class);
        for (int i = 0; i < content.size()/3; i++) {
            deepTest(content.subList(i*3,3*(i+1)));
        }
    }
    public void mergeTest(String contentPath){
        String  resource = getResourceContent("classpath:" + contentPath);
        List content = JsonUtils.parse(resource, List.class);
        for (int i = 0; i < content.size()/3; i++) {
           mergeTest(content.subList(i*3,3*(i+1)));
        }
    }
    public static void clearExtAttrs(JsonType jsonType){
        if(jsonType == null) return;
        //clearGenericTypes(jsonType);
        Map<String,Object> extAttrs = jsonType.getExtAttrs();
        extAttrs.remove(DELETED_FLAG);
        extAttrs.remove(ADD_FLAG);
        extAttrs.remove(DELTA_ATTRS);
        if(jsonType instanceof ComplexJsonType){
            for (JsonType child : ((ComplexJsonType) jsonType).getChildren()) {
                clearExtAttrs(child);
            }
        }
    }
    public void mergeTest(List content){
        Object before = content.get(0);
        Object after = content.get(1);
        Object delta = content.get(2);
        if(before instanceof Map || before == null){
            JsonType result = DeltaHelper.mergeDelta(JsonUtils.cast(before, JsonType.class),
                    JsonUtils.cast(delta, JsonType.class)
            );
            clearExtAttrs(result);

            try{
                final JsonType cast = JsonUtils.cast(after, JsonType.class);
                clearExtAttrs(cast);
                assertEquals(cast,result);
            }catch (Exception e){
                e.printStackTrace();
                assertEquals(JsonUtils.toJSONString(after),JsonUtils.toJSONString(result));
            }

        }else{
            List<JsonType> beforeList = JsonUtils.parseArray(JsonUtils.toJSONString(content.get(0)),JsonType.class);
            List<JsonType> afterList = JsonUtils.parseArray(JsonUtils.toJSONString(content.get(1)),JsonType.class);
            List<JsonType> deltaList = JsonUtils.parseArray(JsonUtils.toJSONString(content.get(2)),JsonType.class);
            List<JsonType> result = DeltaHelper.mergeDeltaList(beforeList, deltaList);
            List<JsonType> expected = JsonUtils.parseArray(JsonUtils.toJSONString(content.get(0)), JsonType.class);
            try{

                assertEquals(result, expected);
            }catch (Exception e){
                e.printStackTrace();
                assertEquals(JsonUtils.toJSONString(result),JsonUtils.toJSONString(expected));
            }

        }

    }

    /**
     *
     * @param content 数组，第0个是before，第1个是after,第2个是diff
     */
    public void deepTest(List content){
        Object o = content.get(0);
        if(o instanceof Map || o == null){
            JsonType result = DeltaHelper.deltaJsonType(JsonUtils.cast(content.get(0), JsonType.class),
                    JsonUtils.cast(content.get(1), JsonType.class),false
            );
            JsonType expected = JsonUtils.cast(content.get(2),JsonType.class);
            try{
                assertDeltaEquals(expected,result);
            }catch (Exception e){
                e.printStackTrace();
                assertEquals(JsonUtils.toJSONString(expected),JsonUtils.toJSONString(result));
            }

        }else{
            List<JsonType> before = JsonUtils.parseArray(JsonUtils.toJSONString(content.get(0)),JsonType.class);
            List<JsonType> after = JsonUtils.parseArray(JsonUtils.toJSONString(content.get(1)),JsonType.class);
            List<JsonType> result = DeltaHelper.deltaList(before, after,false);
            List<JsonType> expected = JsonUtils.parseArray(JsonUtils.toJSONString(content.get(0)), JsonType.class);
            try{

                assertDeltaEquals(result, expected);
            }catch (Exception e){
                e.printStackTrace();
                assertEquals(JsonUtils.toJSONString(result),JsonUtils.toJSONString(expected));
            }

        }
    }
    public void assertMapEquals(Map<String,Object> m1,Map<String,Object> m2){
        if(m1.size() != m2.size()) {
            throw new StdException("size not same").param("m1",m1).param("m2",m2);
        }

        for (Map.Entry<String, Object> entry : m1.entrySet()) {
            if(entry.getValue() instanceof Map){
                assertMapEquals((Map)entry.getValue(),(Map)m2.get(entry.getKey()));
            }else if(!equals(entry.getValue(),m2.get(entry.getKey()))){
                throw new StdException("map not same").param("val1",entry.getValue()).param("val2",m2.get(entry.getKey()));
            }

        }
    }
    public void assertEquals(List<JsonType> c1, List<JsonType> c2){
        for (JsonType child : c1) {
            JsonType found = DeltaHelper.find(c2, child);
            if(found == null){
                throw new StdException("find child not match").param("type1",child).param("type2",found);

            }else {
                assertEquals(child,found);
            }
        }
    }
    public static boolean equals(Object obj1,Object obj2){
        if(obj1==null && obj2 == null) return true;
        if(obj1 == null && Boolean.valueOf(false).equals(obj2)) return true;
        if(Boolean.valueOf(false).equals(obj1) && obj2 == null) return true;
        return ObjectHelper.equals(obj1,obj2);
    }
    public void assertEquals(JsonType type1, JsonType type2){
        for (String needDeltaAttr : needDeltaAttrs) {
            Object val1 = BeanTool.getProp(type1, needDeltaAttr);
            Object val2 = BeanTool.getProp(type2, needDeltaAttr);
            if(val1 instanceof Map){
                assertMapEquals((Map)val1,(Map)val2);
            }else{
                if(!equals(val1,val2)){
                    throw new StdException("type not match").param("type1",val1).param("type2",val2);
                }
            }
        }
        if(!type1.getClass().equals(type2.getClass())){
            throw new StdException("type not match").param("type1",type1).param("type2",type2);
        }

        if(type1 instanceof ComplexJsonType){
            List<JsonType> c1 = children((ComplexJsonType) type1);
            List<JsonType> c2 = children((ComplexJsonType) type2);

            if(c1.size() != c2.size()){
                throw new StdException("size not match").param("type1",type1).param("type2",type2);
            }
            assertEquals(c1,c2);

        }
    }
    public List<JsonType> children(ComplexJsonType type){
        List<JsonType> result = new ArrayList<>();
        if(type.getChildren() != null){
            result.addAll(type.getChildren());
        }
        return result;
    }
    public void assertDeltaEquals(List<JsonType> c1, List<JsonType> c2){
        for (JsonType child : c1) {
            JsonType found = DeltaHelper.find(c2, child);
            if(found == null){
                throw new StdException("find child not match").param("type1",child).param("type2",found);

            }else {
                assertDeltaEquals(child,found);
            }
        }
    }
    public void assertDeltaEquals(JsonType type1, JsonType type2){
        assertMapEquals(type1.getExtAttrs(),type2.getExtAttrs());
        if(!type1.getType().equals(type2.getType())){
            throw new StdException("type not match").param("type1",type1).param("type2",type2);
        }
        if(type1 instanceof ComplexJsonType){
            List<JsonType> c1 = children((ComplexJsonType) type1);
            List<JsonType> c2 = children((ComplexJsonType) type2);

            if(c1.size() != c2.size()){
                throw new StdException("size not match").param("type1",type1).param("type2",type2);
            }
            assertDeltaEquals(c1,c2);

        }
    }
    @Test
    public void testDeltaJsonType(){
        deltaTest("delta/simple-delta.json");
        deltaTest("delta/object-delta.json");
        deltaTest("delta/object-to-array.json");
        deltaTest("delta/string-json-delta.json");
        deltaTest("delta/ref-object-delta.json");
    }
    @Test
    public void testMergeType(){
        mergeTest("delta/simple-delta.json");
        mergeTest("delta/object-delta.json");
        mergeTest("delta/object-to-array.json");
        mergeTest("delta/string-json-delta.json");
        mergeTest("delta/ref-object-delta.json");
    }
}
