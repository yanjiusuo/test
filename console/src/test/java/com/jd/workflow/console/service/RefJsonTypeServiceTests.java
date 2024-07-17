package com.jd.workflow.console.service;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.BeanTool;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.xml.schema.ComplexJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RefJsonTypeServiceTests extends BaseTestCase {
    RefJsonTypeService service = new RefJsonTypeService();
    public static List<String> needDeltaAttrs = Arrays.asList("desc","value","type","extAttrs");





    public void assertMapEquals(Map<String,Object> m1, Map<String,Object> m2){
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
            JsonType found = find(c2, child);
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
                    throw new StdException("type not match").param("val1",val1).param("val2",val2);
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
    public void assertDeltaEquals(List<JsonType> c1, List<JsonType> c2){
        for (JsonType child : c1) {
            JsonType found = find(c2, child);
            if(found == null){
                throw new StdException("find child not match").param("type1",child).param("type2",found);

            }else {
                assertDeltaEquals(child,found);
            }
        }
    }
    public static List<JsonType> children(ComplexJsonType type){
        if(type.getChildren() == null) return new ArrayList<>();
        return type.getChildren();
    }
    public static JsonType find(List<? extends JsonType> types,JsonType found){
        if(found.getName() == null) return null;
        if(types.size() == 1){
            return types.get(0);
        }
        for (JsonType type : types) {
            if(found.getName().equals(type.getName())) return type;
        }
        return null;
    }
    public void assertDeltaEquals(JsonType type1, JsonType type2){
        assertMapEquals(type1.getExtAttrs(),type2.getExtAttrs());
        if(!type1.getClass().equals(type2.getClass())){
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
    public void testRefTest(){
        String content =  getResourceContent("classpath:ref/ref-test.json");
        InstanceRef ref = JsonUtils.parse(content, InstanceRef.class);
        for (int i = 0; i < ref.getTests().size()/2; i++) {
            JsonType before = ref.getTests().get(i*2);
            JsonType after = ref.getTests().get(i*2+1);
             service.fillJsonType(before,ref.getModels());
            System.out.println("========================="+i*2+"===========================");
            System.out.println(JsonUtils.toJSONString(before));
            assertEquals(after,before);
        }

    }

    /**
     * 测试死循环
     */
  /*  @Test
    public void testFillJsonTypeNest(){
        String content =  getResourceContent("classpath:ref/nest.json");
        InstanceRef ref = JsonUtils.parse(content, InstanceRef.class);
        for (int i = 0; i < ref.getTests().size()/2; i++) {
            JsonType before = ref.getTests().get(i*2);
            JsonType after = ref.getTests().get(i*2+1);
            service.fillJsonType(before,ref.getModels());
            System.out.println("========================="+i*2+"===========================");
            System.out.println(JsonUtils.toJSONString(before));
            assertEquals(after,before);
        }
    }*/

    @Data
    public static class InstanceRef{
        Map<String, JsonType> models;
        List<JsonType> tests;
    }
}
