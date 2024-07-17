package com.jd.workflow.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
 /**
 * 用来将java类转成schema，可以参考：https://docs.oracle.com/javase/9/tools/schemagen.htm#JSWOR738
  *
  *  java生成schema:https://docs.oracle.com/javase/tutorial/jaxb/intro/j2schema.html
  *  schemaType与java类型映射：https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html
 * jdk自带的bin里就有schema
 */
public class SchemaGen {
    public static void main(String[] args) throws IOException, InterruptedException {
        String classpath = System.getProperty("java.class.path");

        String cmd = generateSchema(classpath);
        Runtime rt = Runtime.getRuntime();
//            Process pr = rt.exec("cmd /c dir");
//            Process pr = rt.exec("D:/APP/Evernote/Evernote.exe");//open evernote program
        Process pr = rt.exec(cmd);//open tim program
        BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream(),"GBK"));
        String line = null;
        while ((line = input.readLine())!=null){
            System.out.println(line);
        }
        int exitValue = pr.waitFor();
        System.out.println("Exited with error code "+exitValue);


    }
   static String generateSchema(String classpath){
        String javaHome = System.getenv("JAVA_HOME");
        String schemaGenPath =  javaHome+"/bin/schemagen";
        String cmd = String.format("%s -cp %s %s",schemaGenPath,classpath,"com.jd.workflow.entity.FullTyped");
       System.out.println(cmd);
        return cmd;
    }
}
