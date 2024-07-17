package com.jd.workflow.mvel;

import com.jd.workflow.flow.core.expr.CustomMvelExpression;
import com.jd.workflow.soap.common.util.JsonUtils;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.ast.ASTNode;
import org.mvel2.compiler.CompiledExpression;
import org.mvel2.compiler.ExecutableAccessor;
import org.mvel2.compiler.ExpressionCompiler;
import org.mvel2.integration.Interceptor;
import org.mvel2.integration.VariableResolverFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MvelExprTests extends TestCase {
    @Test
    public void testExpr() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("x", 1);
        vars.put("y", 2);
        vars.put("person", new Person("id", "name"));

        // 第一种方式
        Boolean booleanResult = (Boolean) MVEL.eval("/*213*/x == y", vars);
        System.out.println("intResult=" + booleanResult);
        // 第二种方式
        ExecutableAccessor compiled = (ExecutableAccessor) MVEL.compileExpression("x * y");
        int intResult = (Integer) MVEL.executeExpression(compiled, vars);
        System.out.println("intResult=" + intResult);

        String str = (String) MVEL.eval("person.attrs.put('sid',1);person.name", vars);
        System.out.println("str=" + str);

    }

    @Test
    public void testExpr111() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("x", 1);
        vars.put("y", 2);
        Person person = new Person("id", "name");
        person.getAttrs().put("id", "312");
        vars.put("person", person);

        ParserContext ctx = new ParserContext();

        ctx.addPackageImport("java.util");
        ctx.addPackageImport("java.lang");

        Serializable compiled = MVEL.compileExpression("Map a = person.getAttrs();a;", ctx);
        Map map = (Map) MVEL.executeExpression(compiled, vars);
        System.out.println("intResult=" + map);

    }

    @Test
    public void testExprCompile() {

        ParserContext ctx = new ParserContext();
        ctx.addPackageImport("java.util");
        ctx.addPackageImport("java.lang");

        Serializable compiled = MVEL.compileExpression("var a = 1;a==1", ctx);
        Boolean map = (Boolean) MVEL.executeExpression(compiled, new HashMap());
        System.out.println("intResult=" + map);

    }
    @Test
    public void testImportPck() {

        ParserContext ctx = new ParserContext();
        /*ctx.addPackageImport("java.util");
        ctx.addPackageImport("java.lang");*/
        ctx.addImport(StringUtils.class);

        Serializable compiled = MVEL.compileExpression("StringUtils.isEmpty('');", ctx);
        Boolean map = (Boolean) MVEL.executeExpression(compiled, new HashMap());
        System.out.println("intResult=" + map);

    }
    protected String getResourceContent(String path){

        try {
            File file = ResourceUtils.getFile(path);
            return IOUtils.toString(new FileInputStream(file),"utf-8");
        } catch (Exception e) {
            return null;
        }
    }
    @Test
    public void testMap() {
        String script = getResourceContent("classpath:mvel/script.mvel");
        ParserContext ctx = new ParserContext();
        ctx.addPackageImport("java.util");
        ctx.addPackageImport("java.lang");

        CustomMvelExpression compiled = CustomMvelExpression.mvel(script, true);
        Object map =  compiled.evaluate( new HashMap());
        System.out.println("intResult=" + map);

    }
    /**

     */
    @Test
    public void testDateFormat(){
        String s = "utils.formatDate(\"2020-01-01\",\"yyyy-MM-dd\",\"yyyy年MM月dd日\")";
        CustomMvelExpression mvel = CustomMvelExpression.mvel(s);
        Map<String,Object> map = new HashMap<>();
        Object value = mvel.evaluate(map);
        System.out.println(value);
        assertEquals("2020年01月01日",value);
    }
    @Test
    public void testNumberFormat(){
        String s = "utils.valueOf(\"123\").toInt()";
        CustomMvelExpression mvel = CustomMvelExpression.mvel(s);
        Map<String,Object> map = new HashMap<>();
        Object value = mvel.evaluate(map);
        System.out.println(value);
        assertEquals(123,value);
    }

    @Test
    public void testMapping(){

        String s = "Map map = {\n" +
                "                \"1\":\"男\",\n" +
                "                \"2\":\"女\"\n" +
                "        };\n" +
                "        map[\"1\"]";
        CustomMvelExpression mvel = CustomMvelExpression.mvel(s);
        Map<String,Object> map = new HashMap<>();
        Object value = mvel.evaluate(map);
        System.out.println(value);
        assertEquals("男",value);
    }

    @Test
    public void testNullPropertyHandler() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("x", 1);
        vars.put("y", 2);
        Person person = new Person("id", "name");
        person.getAttrs().put("id", "312");
        vars.put("person", person);

        Map<String, Object> personMap = JsonUtils.cast(person, Map.class);

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("person", personMap);

        Map<String, Object> child = new HashMap<>();
        child.put("sid", 123);
        personMap.put("child", child);

        vars.put("props", props);

        ParserContext ctx = new ParserContext();
        ctx.addPackageImport("java.util");
        ctx.addPackageImport("java.lang");


        Serializable compiled = MVEL.compileExpression("var a = props.person.name;", ctx);
        Object map = MVEL.executeExpression(compiled, vars);
        System.out.println("intResult=" + map);
    }

    @Test
    public void testPropertyHandler() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("x", 1);
        vars.put("y", 2);
        Map person = new LinkedHashMap();
        person.put("id", "312");
        vars.put("person", person);

        ParserContext ctx = new ParserContext();
        ctx.addPackageImport("java.util");
        ctx.addPackageImport("java.lang");

        Serializable compiled = MVEL.compileExpression("var a = person.id;", ctx);
        Map map = (Map) MVEL.executeExpression(compiled, vars);
        System.out.println("intResult=" + map);
    }

    public static class Person {
        String name;
        String id;
        Map<String, Object> attrs = new HashMap<>();

        public Person(String name, String id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Map<String, Object> getAttrs() {
            return attrs;
        }

        public void setAttrs(Map<String, Object> attrs) {
            this.attrs = attrs;
        }
    }
}
