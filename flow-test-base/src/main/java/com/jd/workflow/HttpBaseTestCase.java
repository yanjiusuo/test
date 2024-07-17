package com.jd.workflow;

import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


public class HttpBaseTestCase extends BaseTestCase {
    protected static int port = 6010;
    protected static String SERVICE_URL= "http://127.0.0.1:"+port ;
    protected static HttpServer server = null;
    @BeforeClass

    public static void before() throws Exception {

        server = HttpTestServer.run(port);
    }
    @AfterClass

    public static void after() throws Exception {

        if(server != null){
            server.stop(0);
        }

    }
}
