package com.jd.workflow.console.service.doc.importer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.ConsoleApplication;
import com.jd.workflow.metrics.client.RequestClient;
import com.jd.workflow.soap.common.util.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import springfox.documentation.RequestHandler;
import springfox.documentation.spi.service.RequestHandlerProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaCodeParserTests extends BaseTestCase {
    @Test
    public void testParseJava() throws FileNotFoundException {
        File appControllerFile = new File("D:\\github-git\\interface-transform\\dev-data-flow\\console\\src\\main\\java\\com\\jd\\workflow\\console\\controller\\AppInfoController.java");
        final CompilationUnit unit = StaticJavaParser.parse(appControllerFile);
        unit.addImport(ApiOperation.class);
        unit.findAll(MethodDeclaration.class).stream().filter(vs->{
            return vs.getName().getIdentifier().equals("addApp");
        }).forEach(vs->{
            NormalAnnotationExpr annotationExpr = new NormalAnnotationExpr();
            annotationExpr.setName(new Name("ApiOperation"));
            NodeList<MemberValuePair> pairs = new NodeList<>();
            MemberValuePair value = new MemberValuePair();
            value.setName("value");
            value.setValue(new StringLiteralExpr("描述信息fjdlksljksdfjkldsfj"));
            pairs.add(value);

            annotationExpr.setPairs(pairs);
            vs.addAnnotation(annotationExpr);

            for (Parameter parameter : vs.getParameters()) {
                parameter.getComment();
            }
        });
        System.out.println(unit.toString());
    }

}
