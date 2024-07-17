package com.jd.workflow.console.service.doc.importer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
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
import com.jd.workflow.soap.common.util.StringHelper;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {ConsoleApplication.class},                                          // spring boot 的启动类
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT      // 启动容器使用随机端口号, 可以不用
)
public class LocalDebugJApiDataImport extends BaseTestCase {
    RequestClient requestClient = null;
    @Autowired
    List<RequestHandlerProvider> handlerProviders;
    List<RequestHandler> requestHandlers = new ArrayList<>();
    Map<String,HandlerMethod> path2Methods = new HashMap<>();
    @Before
    public void setUp(){
        Map<String,Object> headers = new HashMap<>();
        headers.put("Cookie","sso.jd.com=BJ.9978B49EA0D446E0FEF15788CBC97F5B.6920221219092652");
        requestClient = new RequestClient("http://j-api.jd.com",headers);
        for (RequestHandlerProvider handlerProvider : handlerProviders) {
            requestHandlers.addAll(handlerProvider.requestHandlers());
        }
        collectRequestHandlers(requestHandlers,path2Methods);
    }
    private static void collectRequestHandlers(List<RequestHandler> requestHandlers, Map<String, HandlerMethod> path2Methods) {
        for (RequestHandler requestHandler : requestHandlers) {
            HandlerMethod handlerMethod = requestHandler.getHandlerMethod();
            RequestMappingInfo requestMapping = requestHandler.getRequestMapping();
            for (String pattern : requestMapping.getPatternsCondition().getPatterns()) {
                /*if(!requestMapping.getMethodsCondition().getMethods().isEmpty()){
                    for (RequestMethod method : requestMapping.getMethodsCondition().getMethods()) {
                        path2Methods.put((pattern+method.name()).toLowerCase(),handlerMethod);
                    }
                }else{*/
                    path2Methods.put(pattern.toLowerCase(),handlerMethod);
                //}
            }
        }
    }
    public void collectJavaSourceFiles(File root,Map<String,String> javaSources){
        if(root.isFile()){
            if(root.getName().endsWith(".java")){
                javaSources.put(StringHelper.firstPart(root.getName(),'.'),root.getAbsolutePath());
            }
            return;
        }
        for (File file : root.listFiles()) {
            collectJavaSourceFiles(file,javaSources);
        }
    }
    @Test
    public void importJApiDataToProject(){
        Map<String,String> javaSourceFiles = new HashMap<>();
        collectJavaSourceFiles(new File("D:\\github-git\\interface-transform\\dev-data-flow\\console\\src\\main\\java"),javaSourceFiles);




        Map<String,Object> params = new HashMap<>();
        params.put("projectID",1838);
         String result = requestClient.post("/Project/getProject", params, null);
        Map<String,Object> map  = JsonUtils.parse(result,Map.class);
        Map<String,Object> projectInfo = (Map<String, Object>) map.get("projectInfo");
        Map<String,Object> apiGroupSortTree = (Map<String, Object>) projectInfo.get("apiGroupSortTree");
        List<Map<String,Object>> apiArrForSort = (List<Map<String, Object>>) apiGroupSortTree.get("apiArrForSort");
        for (Map<String, Object> apiInfo : apiArrForSort) {
            String apiUrl = (String) apiInfo.get("apiURI");
            apiUrl = StringUtils.replace(apiUrl,"{host}","").trim();
            String apiName = (String) apiInfo.get("apiName");
            HandlerMethod handlerMethod = path2Methods.get(apiUrl.toLowerCase());
            if(handlerMethod != null){
                String simpleClassName =  handlerMethod.getBeanType().getSimpleName();
                String javaSource = javaSourceFiles.get(simpleClassName);
                if(!StringUtils.isEmpty(javaSource)){
                    modifyJavaSource(javaSource,handlerMethod.getMethod().getName(),apiName);
                }
            }
        }


    }
    private void modifyJavaSource(String source,String methodName,String methodDesc){
        File appControllerFile = new File(source);
        final CompilationUnit unit;
        try {
            unit = StaticJavaParser.parse(appControllerFile);
            unit.addImport(ApiOperation.class);
            unit.findAll(MethodDeclaration.class).stream().filter(vs->{
                return vs.getName().getIdentifier().equals(methodName);
            }).forEach(vs->{
                NormalAnnotationExpr annotationExpr = new NormalAnnotationExpr();
                annotationExpr.setName(new Name("ApiOperation"));
                NodeList<MemberValuePair> pairs = new NodeList<>();
                MemberValuePair value = new MemberValuePair();
                value.setName("value");
                value.setValue(new StringLiteralExpr(methodDesc));
                pairs.add(value);

                annotationExpr.setPairs(pairs);
                vs.addAnnotation(annotationExpr);

                for (Parameter parameter : vs.getParameters()) {
                    parameter.getComment();
                }
            });
            final FileWriter writer = new FileWriter(source);
            IOUtils.write(unit.toString(), writer);
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
