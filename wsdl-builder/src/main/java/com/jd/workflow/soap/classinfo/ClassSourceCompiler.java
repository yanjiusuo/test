package com.jd.workflow.soap.classinfo;

import com.jd.workflow.soap.classloader.MemoryClassLoader;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.utils.StringHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.util.SystemPropertyAction;
import org.apache.cxf.helpers.FileUtils;
import org.apache.cxf.helpers.JavaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class ClassSourceCompiler {
    static final Logger LOG = LoggerFactory.getLogger(ClassSourceCompiler.class);
    private String tmpdir = SystemPropertyAction.getProperty("java.io.tmpdir");
    public MemoryClassLoader compile(Map<String,String> source)  {
       try{
            return _compile(source);
       }catch (Exception e){
           throw new StdException("compile_source_fail:source="+source.keySet(),e);
       }
    }
    private MemoryClassLoader _compile(Map<String,String> source) throws ClassNotFoundException {
        String stem = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
        File src = new File(tmpdir, stem + "-src");
        if (!src.mkdir()) {
            throw new IllegalStateException("Unable to create working directory " + src.getPath());
        }
        try {
            for (Map.Entry<String, String> entry : source.entrySet()) {
                String pkgName = StringHelper.getPkgNameByClassName(entry.getKey());
                pkgName = StringUtils.replace(pkgName, ".", "/");
                File dir = new File(src,pkgName);
                if(!dir.exists()){
                    if (!dir.mkdirs()) {
                        throw new IllegalStateException("Unable to create working directory " + src.getPath());
                    }
                }
                File javaSource =  new File(dir,StringHelper.simpleClassName(entry.getKey())+".java");
                try(FileWriter writer = new FileWriter(javaSource)) {
                    writer.write(entry.getValue());
                }
            }

        } catch (Exception e) {
            throw new IllegalStateException("Unable to write generated Java files for schemas: "
                    + e.getMessage(), e);
        }
        File classDir = new File(tmpdir, stem + "-classDir");
        if (!classDir.mkdir()) {
            throw new IllegalStateException("Unable to create working directory " + classDir.getPath());
        }
        StringBuilder classPath = new StringBuilder();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            setupClasspath(classPath, classLoader);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        List<File> srcFiles = FileUtils.getFilesRecurseUsingSuffix(src, ".java");
        if (!srcFiles.isEmpty() && !compileJavaSrc(classPath.toString(), srcFiles, classDir.toString())) {
            LOG.info("cound_not_compile_src:keys={}",source.keySet());
            throw new StdException("class.err_compile_source").param("source",srcFiles);
        }
        FileUtils.removeDir(src);
        final URL[] urls;
        try {
            urls = new URL[] {classDir.toURI().toURL()};
        } catch (MalformedURLException mue) {
            throw new IllegalStateException("Internal error; a directory returns a malformed URL: "
                    + mue.getMessage(), mue);
        }
        final ClassLoader cl = ClassLoaderUtils.getURLClassLoader(urls, classLoader);
        MemoryClassLoader memoryClassLoader = new MemoryClassLoader(urls);
        for (Map.Entry<String, String> entry : source.entrySet()) {
            memoryClassLoader.
                    loadClass(entry.getKey());
        }

        FileUtils.removeDir(classDir);
        return memoryClassLoader;
    }
    static void setupClasspath(StringBuilder classPath, ClassLoader classLoader)
            throws URISyntaxException, IOException {

        ClassLoader scl = ClassLoader.getSystemClassLoader();
        ClassLoader tcl = classLoader;
        do {
            if (tcl instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader)tcl).getURLs();
                if (urls == null) {
                    urls = new URL[0];
                }
                for (URL url : urls) {
                    if (url.getProtocol().startsWith("file")) {
                        File file = null;
                        // CXF-3884 use url-decoder to get the decoded file path from the url
                        try {
                            if (url.getPath() == null) {
                                continue;
                            }
                            file = new File(URLDecoder.decode(url.getPath(), "utf-8"));
                        } catch (UnsupportedEncodingException uee) {
                            // ignored as utf-8 is supported
                        }

                        if (null != file && file.exists()) {
                            classPath.append(file.getAbsolutePath())
                                    .append(System
                                            .getProperty("path.separator"));

                            if (file.getName().endsWith(".jar")) {
                                addClasspathFromManifest(classPath, file);
                            }
                        }
                    }
                }
            } else if (tcl.getClass().getName().contains("weblogic")) {
                // CXF-2549: Wrong classpath for dynamic client compilation in Weblogic
                try {
                    Method method = tcl.getClass().getMethod("getClassPath");
                    Object weblogicClassPath = method.invoke(tcl);
                    classPath.append(weblogicClassPath)
                            .append(File.pathSeparator);
                } catch (Exception e) {
                    LOG.error("append_class_fail", e);
                }
            }
            tcl = tcl.getParent();
            if (null == tcl) {
                break;
            }
        } while(!tcl.equals(scl.getParent()));
    }

    static void addClasspathFromManifest(StringBuilder classPath, File file)
            throws URISyntaxException, IOException {

        try (JarFile jar = new JarFile(file)) {
            Attributes attr = null;
            if (jar.getManifest() != null) {
                attr = jar.getManifest().getMainAttributes();
            }
            if (attr != null) {
                String cp = attr.getValue("Class-Path");
                while (cp != null) {
                    String fileName = cp;
                    int idx = fileName.indexOf(' ');
                    if (idx != -1) {
                        fileName = fileName.substring(0, idx);
                        cp = cp.substring(idx + 1).trim();
                    } else {
                        cp = null;
                    }
                    URI uri = new URI(fileName);
                    File f2;
                    if (uri.isAbsolute()) {
                        f2 = new File(uri);
                    } else {
                        f2 = new File(file, fileName);
                    }
                    if (f2.exists()) {
                        classPath.append(f2.getAbsolutePath());
                        classPath.append(File.pathSeparator);
                    }
                }
            }
        }
    }
    protected boolean compileJavaSrc(String classPath, List<File> srcList, String dest) {
        org.apache.cxf.common.util.Compiler javaCompiler
                = new org.apache.cxf.common.util.Compiler();

        javaCompiler.setClassPath(classPath);
        javaCompiler.setOutputDir(dest);
        if (JavaUtils.isJava9Compatible()) {
            javaCompiler.setTarget("9");
        } else {
            javaCompiler.setTarget("1.8");
        }

        boolean result = javaCompiler.compileFiles(srcList);
        if(!result){
            LOG.error("java.err_compile_source:src={},errors={}",srcList.get(0),javaCompiler.getErrors());
            LOG.warn("java.err_compile_source:src={},errors={}",srcList.get(0),javaCompiler.getWarnings()
            );
        }
        return result;
    }
}
