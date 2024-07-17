package com.jd.workflow.soap.wsdl;

import com.jd.workflow.soap.classinfo.model.ClassInfo;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.utils.StringHelper;
import com.jd.workflow.soap.wsdl.param.Param;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * 根据模型信息生成wsdl定义信息
 */
@Data
public class WsdlModelInfo {
    /**
     * 目标名称空间，默认是java类的包名
     */
    String targetNamespace;
    /**
     * 服务名称
     */
    String serviceName;
    List<ServiceMethodInfo> methods;
    /**
     * 断点地址
     */
    String endpointUrl;

    /**
     * 收集签名方法用到的所有类
     * @return
     */
    public Collection<ClassInfo> getAllClass(){

        String pkgName = StringHelper.getPkgNameByNamespace(targetNamespace);
        Map<String,Param> classes = new HashMap<>();

        for (ServiceMethodInfo method : getMethods()) {
            if(method.getOutParam().isObject()){
                //classes.add(method.getOutParam().toClass(pkgName));
                collectClassInfo(method.getOutParam(),pkgName,null,classes);
            }
            for (Param inputParam : method.getInputParams()) {
               /* if(inputParam.isObject()){
                    classes.add(method.getOutParam().toClass(pkgName));
                }*/
                collectClassInfo(inputParam,pkgName,null,classes);
            }
        }
        List<ClassInfo> classInfos = new ArrayList<>();
        for (Map.Entry<String, Param> entry : classes.entrySet()) {
            ClassInfo classInfo = entry.getValue().toClass(pkgName);
            classInfo.setName(entry.getKey());
            classInfos.add(classInfo);
        }

        return classInfos;
    }

    private String resolveNoConflictClassName(Param param,String parentName,Map<String,Param> classes){
        String className = param.getClassName();
        if(StringUtils.isEmpty(className)){ // 获取不重复的className
            className = StringUtils.capitalize(param.getName());
            if(!com.jd.workflow.soap.common.util.StringHelper.isValidVarName(className)){
                className = parentName + "Item";
            }
            if(classes.containsKey(className)){
                if(!StringUtils.isEmpty(parentName)){
                    String newClassName = StringUtils.capitalize(parentName)+StringUtils.capitalize(param.getName());
                    if(!classes.containsKey(newClassName)){
                        className = newClassName;
                    }
                }
            }

            int i = 1;
            if(classes.containsKey(className)){
                do{
                    className = className + i;
                    i++;
                }while (classes.containsKey(className));
            }
        }
        return className;
    }
    private void collectClassInfo(Param param,String pkgName,String parentName,Map<String,Param> classes){
        if(param.isObject()){
            if(param.getName() == null){
                throw new StdException("wsdl.err_miss_param_name")
                        .param("pkgName",pkgName)
                        .param("parentName",parentName);
            }
            String className = resolveNoConflictClassName(param,parentName,classes);
            param.setClassName(className);
            classes.put(className,param);
            for (Param child : param.getChildren()) {
                collectClassInfo(child,pkgName,className,classes);
            }
        }else if(param.getParamType().isArray()){
            String arrayName = param.getName();

            for (Param child : param.getChildren()) {
                if(child.isObject() && StringUtils.isEmpty(child.getClassName()) && (StringUtils.isEmpty(child.getName())
                    || !com.jd.workflow.soap.common.util.StringHelper.isValidVarName(child.getName())
                )){
                    child.setName(arrayName+"Item");
                }
                collectClassInfo(child,pkgName, parentName,classes);
            }
        }
    }
}
