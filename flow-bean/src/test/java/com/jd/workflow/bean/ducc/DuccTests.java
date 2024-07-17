package com.jd.workflow.bean.ducc;

import com.jd.laf.config.Configuration;
import com.jd.laf.config.ConfiguratorManager;
import com.jd.laf.config.Property;
import com.jd.laf.config.Resource;
import com.jd.workflow.BaseTestCase;
import com.jd.workflow.flow.core.bean.InitParam;
import org.junit.Test;

public class DuccTests extends BaseTestCase {
    @Test
    public void init() {

        String appName = "myapp_test" ;
//resource uri format => ucc://{app_name}:{token}@{domain}:{port}/v1/namespace/{namespace}/config/{configuration}/profiles/{profiles}?longPolling=60000&necessary=false
        String uri = "ucc://jd-components:c44cb57c-c69e-41d9-a8cf-45633343b620@test.ducc.jd.local/v1/namespace/flow_assemble/config/app.integration-paas/profiles/dev?longPolling=60000&necessary=false" ;

//重要：获取内置的 ConfiguratorManager 单例对象（使用场景：如果项目内只有自己依赖了 DUCC）
        ConfiguratorManager configuratorManager = ConfiguratorManager.getInstance();
//重要：new 一个 manager 对象（使用场景：如果项目内依赖了其它 jar，并且这些 jar 依赖了 DUCC）
        //ConfiguratorManager configuratorManager = new ConfiguratorManager();

        //设置appName，jone或者jdos部署可自动获取，无需配置
        configuratorManager.setApplication(appName);
//创建资源对象，此处直接使用ducc远程 ，第一个resource ，name=resource01，第二个resource，name=resource02... (resource name 用于区分资源对象，可以不自己定义名称，需要确保不重复)
        Resource resource = new Resource("ucc", uri);
//给配置管理器添加管理的资源
        configuratorManager.addResource(resource);
//启动之后才可以获取配置
        try {
            configuratorManager.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
// 在调用 start 之后不能再调用 addResource 去添加资源，如果要添加资源需要先 stop 再调用 addResource ，然后再调用 start 方法
//获取配置 方式1 (获取合并后指定配置)
        Property property1 = configuratorManager.getProperty("history");
        System.out.println("jdbc.url:" + property1.getString());
//获取配置 方式2 (获取指定配置源下指定配置)
        Property property2 = configuratorManager.getProperty("resource01", "jdbc.url");
        System.out.println("jdbc.url:" + property2.getString());
//获取配置 方式3 (获取指定配置源下配置集合)
        Configuration configuraiton = configuratorManager.getConfiguration("resource01");
        System.out.println("resource configuration:" + configuraiton);
        System.out.println("jdbc.url:" + configuraiton.getProperty("jdbc.url").getString());

//java进程退出时，可进行关闭
        /*configuratorManager.stop();*/

    }
    public static void testDuccManagerOrConfigurationIsNullWithNotExist() throws Exception {
        String appName = "jd-components";
        String token = "c44cb57c-c69e-41d9-a8cf-45633343b620";
        String namespace = "flow_assemble";
        //String configCode = "APP.sure";
        String configCode = "app.flow-test";
        String env = "dev";
        String uri = "ucc://" + appName + ":" + token + "@ducc.jd.local/v1/namespace/" + namespace + "/config/" + configCode + "/profiles/" + env + "?longPolling=5000&necessary=true";

        ConfiguratorManager configuratorManager = ConfiguratorManager.getInstance();
        configuratorManager.setApplication(appName);
        Resource resource = new Resource("ucc", uri);
        configuratorManager.addResource(resource);

        try {
            configuratorManager.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Configuration configuration = configuratorManager.getConfiguration("ucc");//TODO:待验证
    }

    public static void main(String[] args) throws Exception {

        testDuccManagerOrConfigurationIsNullWithNotExist();
    }
}
