package com.jd.workflow.console.code;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class JavaCodeExporter {
    private static void collectFiles(File root, List<File> files){
        if(root.isFile()){
            if(root.getName().endsWith(".java")){
                files.add(root);
            }

        }else {
            for (File file : root.listFiles()) {
                collectFiles(file,files);
            }
        }


    }
    public static void main(String[] args) throws Exception {
        File root = new File("D:\\github-git\\interface-transform\\dev-data-flow\\console\\src\\main\\java");
        List<File> sources = new ArrayList<>();
        collectFiles(root,sources);
        StringBuilder sb = new StringBuilder();
        for (File source : sources) {
            sb.append(IOUtils.toString(new FileInputStream(source)));
            sb.append("\n");
        }
        File output = new File("d:/tmp/code.java");
        FileWriter writer = new FileWriter(output);
        writer.write(sb.toString());
        writer.flush();


    }
}
