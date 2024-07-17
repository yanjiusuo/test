package com.jd.workflow.console.service.parser;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/4/24
 */
public class ClassReference {
    String className;
    boolean isShortClassName;

    List<ClassReference> children = new ArrayList<>();

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<ClassReference> getChildren() {
        return children;
    }

    public void addChild(ClassReference reference){
        children.add(reference);
    }

    public boolean isShortClassName() {
        return className.indexOf(".") == -1;
    }
    public boolean isList(){
        return "List".equals(className) || "Set".equalsIgnoreCase(className);
    }
    public boolean isMap(){
        return "Map".equals(className) ;
    }



    /**
     * 解析泛型表达式
     * @param text Pair<A,ApiResult<B>>
     * @return
     */
    public static ClassReference parse(String text){
        StringBuilder sb = new StringBuilder();
        ClassReference reference = new ClassReference();
        ClassReference current = reference;
        if(text.indexOf(">") == -1){
            reference.setClassName(text);
            return reference;
        }

        Stack<ClassReference> stack = new Stack<>();
        //stack.add(reference);

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c){
                case '<':
                    if(!stack.isEmpty() ){
                        stack.peek().addChild(current);
                    }
                    stack.push(current);
                    current.setClassName(sb.toString());
                    current = new ClassReference();
                    sb = new StringBuilder();

                    break;
                case ',':
                    current.setClassName(sb.toString());
                    sb = new StringBuilder();
                    stack.peek().addChild(current);
                    current = new ClassReference();
                    break;
                case '>':
                    ClassReference parent = stack.pop();
                    String className = sb.toString();
                    if(StringUtils.isEmpty(className)) {

                        break;
                    }
                    current.setClassName(sb.toString());
                    parent.addChild(current);
                    current = new ClassReference();
                    sb = new StringBuilder();


                    break;
                default:
                    sb.append(c);
            }
        }

        if(stack.size() != 0) return null;
        return reference;
    }

    public static void main(String[] args) {

       /* ClassReference parse = ClassReference.parse("com.jd.Pair<a.Left,ApiResult<B>>");
        System.out.println(parse);*/
        ClassReference reference1 = ClassReference.parse("ApiResult<B>");
        System.out.println(reference1);
        ClassReference reference2 = ClassReference.parse("ApiResult<B,C>");
        System.out.println(reference2);
    }
}
