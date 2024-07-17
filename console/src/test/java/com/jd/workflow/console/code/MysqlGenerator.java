package com.jd.workflow.console.code;

import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;

/**
 * <p>
 * mysql 代码生成器演示例子
 * </p>
 *
 * @author jobob
 * @since 2018-09-12
 */
public class MysqlGenerator {

    @Data
    @Accessors(chain = true)
    static
    class param{
        String businessName = "local/";
        String businessNameDot = ".local";

        String projectPath =  System.getProperty("user.dir");
        String parent ="com.jd.workflow.console";
        String entity ="entity"+businessNameDot;
        String serviceImpl = "service"+businessNameDot+".impl";
        String service = "service"+businessNameDot;
        String mapper="dao.mapper"+businessNameDot;
        String controller="controller"+businessNameDot;
        String moduleName="";

        String entityPath = projectPath+"/console/src/main/java/com/jd/workflow/console/entity/"+businessName;
        String servicePath = projectPath+"/console/src/main/java/com/jd/workflow/console/service/"+businessName;
        String serviceImplPath = projectPath+"/console/src/main/java/com/jd/workflow/console/service/"+businessName+"impl/";
        String mapperPath = projectPath+"/console/src/main/java/com/jd/workflow/console/dao/mapper/"+businessName;
        String xmlPath = projectPath+"/console/src/main/resources/mapper/"+businessName;
        String controllerPath = projectPath+"/console/src/main/java/com/jd/workflow/console/controller/"+businessName;

        String [] include = new String [0] ;

        /**
         * 驱动连接的URL
         */
        private String url;
        /**
         * 驱动名称
         */
        private String driverName;
        /**
         * 数据库连接用户名
         */
        private String username;
        /**
         * 数据库连接密码
         */
        private String password;


    }

    /**
     * RUN THIS
     */
    public static void main(String[] args) {
        param param = new param()
                .setInclude(new String []{"local_test_record","r_requirement_case",})
                .setUrl("jdbc:mysql://gate6.local.jed.jddb.com:3306/flow_db?useUnicode=true&serverTimezone=GMT&useSSL=false&characterEncoding=utf8")
//                .setUrl("jdbc:mysql://gate6.local.jed.jddb.com:3306/flow_db?characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&autoReconnect=true&allowMultiQueries=true&useSSL=false&serverTimezone=Asia/Shanghai\n")
                .setDriverName("com.mysql.jdbc.Driver")
                .setUsername("flow_db_admin")
                .setPassword("mW40LF42tr4fDqFs");


        doCreate(param);
    }

    private static void doCreate(param param) {
        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = param.getProjectPath();
        gc.setOutputDir(projectPath);
        gc.setAuthor("sunchao81");
        gc.setOpen(false);
        gc.setSwagger2(true); //开启Swagger2模式
        gc.setDateType(DateType.ONLY_DATE);
        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl(param.getUrl());
        dsc.setDriverName(param.getDriverName());
        dsc.setUsername(param.getUsername());
        dsc.setPassword(param.getPassword());
        mpg.setDataSource(dsc);

        // 包配置
        PackageConfig pc = new PackageConfig();
        //设置个模块包名
        pc.setModuleName(param.getModuleName());
        pc.setParent(param.getParent());
        pc.setEntity(param.getEntity());
        pc.setServiceImpl(param.getServiceImpl());
        pc.setService(param.getService());
        pc.setMapper(param.getMapper());
        pc.setController(param.getController());

        HashMap hashMap = new HashMap();
        //设置各层文件数据路径
        hashMap.put(ConstVal.SERVICE_PATH,param.getServicePath());
        hashMap.put(ConstVal.CONTROLLER_PATH,param.getControllerPath());
        hashMap.put(ConstVal.SERVICE_IMPL_PATH,param.getServiceImplPath());
        hashMap.put(ConstVal.ENTITY_PATH,param.getEntityPath());
        hashMap.put(ConstVal.MAPPER_PATH,param.getMapperPath());
        hashMap.put(ConstVal.XML_PATH,param.getXmlPath());

        pc.setPathInfo(hashMap);

        mpg.setPackageInfo(pc);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
//        strategy.setSuperEntityClass("com.baomidou.mybatisplus.samples.generator.common.BaseEntity");
        strategy.setEntityLombokModel(true);
//        strategy.setSuperControllerClass("com.baomidou.mybatisplus.samples.generator.common.BaseController");

        strategy.setInclude(param.include);
//        strategy.setSuperEntityColumns("id");
        strategy.setEntityTableFieldAnnotationEnable(true);
        strategy.setControllerMappingHyphenStyle(true);
        strategy.setChainModel(true);
        //表前缀
//        strategy.setTablePrefix("sn_");
        mpg.setStrategy(strategy);
        mpg.setTemplateEngine(new FreemarkerTemplateEngine());
        mpg.execute();
    }

}