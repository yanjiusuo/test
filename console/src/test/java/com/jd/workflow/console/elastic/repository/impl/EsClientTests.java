package com.jd.workflow.console.elastic.repository.impl;

import com.jd.workflow.BaseTestCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RunWith(JUnit4.class)
public class EsClientTests extends BaseTestCase {
    private static Client client;
    private static final String SecurityKey = "request.headers.Authorization";//固定值

    public static synchronized Client getClient(String clusterName, String ipPortList,String userName,String password) {
        try {
            if (clusterName == null || clusterName.isEmpty() || ipPortList == null || ipPortList.isEmpty()) {
                throw new Exception("集群名，连接地址没有设置");
            }

            //设置集群的名字
            Settings settings = Settings.builder()
                    .put("cluster.name", clusterName)
                    .put(SecurityKey,basicAuthHeaderValue(userName,password))
                    .put("client.transport.sniff", false)
            .put("client.transport.ping_timeout", "2s")
                    .put("transport.tcp.connect_timeout", "1s")
                    .build();

//                new NodeClient(settings)
            PreBuiltTransportClient tempClient = new PreBuiltTransportClient(settings);//初始化client较老版本发生了变化，此方法有几个重载方法，初始化插件等。
            //此步骤添加IP，至少一个，其实一个就够了，因为添加了自动嗅探配置
            //tempClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));

            String[] ipPortArr = ipPortList.split(";");//逗号分隔
            for (String ipPort : ipPortArr){
                String[] ip_port = ipPort.split(":");//冒号分隔
                if(ip_port.length == 2){
                    tempClient.addTransportAddress(new TransportAddress(InetAddress.getByName(ip_port[0]), Integer.parseInt(ip_port[1])));
                }
            }


            //创建集群client并添加集群节点地址
            //创建集群client并添加集群节点地址,可以addTransportAddress()多个ip和端口，增加连接的稳定性。
//            TransportClient client = TransportClient.builder().settings(settings).build();
//            String[] ipPortArr = ipPortList.split(",");//逗号分隔
//            for (String ipPort : ipPortArr){
//                String[] ip_port = ipPort.split(":");//冒号分隔
//                if(ip_port.length == 2){
//                    client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip_port[0]), Integer.parseInt(ip_port[1])));
//                }
//            }
            client = tempClient;
            return client;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 基础的base64生成
     * @param username 用户名
     * @param passwd 密码
     * @return
     */
    private static String basicAuthHeaderValue(String username, String passwd) {
        CharBuffer chars = CharBuffer.allocate(username.length() + passwd.length() + 1);
        byte[] charBytes = null;
        try {
            chars.put(username).put(':').put(passwd.toCharArray());
            charBytes = toUtf8Bytes(chars.array());

            String basicToken = Base64.getEncoder().encodeToString(charBytes);
            return "Basic " + basicToken;
        } finally {
            Arrays.fill(chars.array(), (char) 0);
            if (charBytes != null) {
                Arrays.fill(charBytes, (byte) 0);
            }
        }
    }
    public static Client getClient(){
        if(client != null) return client;
        client = getClient("jiesi-7.5","11.158.105.153:20100","jiesi-7.5","9CCBDFB47A07D620");
        return client;
    }
    public static byte[] toUtf8Bytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0);
        return bytes;
    }

    public static void main(String[] args) throws UnknownHostException {
        String source = "{\n" +
                "  \"settings\": {\n" +
                "    \"number_of_shards\": 1,\n" +
                "    \"number_of_replicas\": 1\n" +
                "  },\n" +
                "  \"mappings\":{\n" +
                "      \"_doc\": {\n" +
                "        \"properties\":{\n" +
                "          \"interfaceId\":{\n" +
                "            \"type\":\"long\"\n" +
                "          },\n" +
                "          \"name\":{\n" +
                "            \"type\": \"keyword\"\n" +
                "          },\n" +
                "          \"erps\": {\n" +
                "            \"type\": \"text\",\n" +
                "            \"analyzer\": \"whitespace\"\n" +
                "          },\n" +
                "          \"serviceCode\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          },\n" +
                "          \"visibility\":  {\n" +
                "            \"type\": \"keyword\"\n" +
                "          },\n" +
                "          \"appName\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          },\n" +
                "          \"appCode\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          },\n" +
                "          \"deptName\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "   }\n" +
                "}\n";
         client = getClient("jiesi-7.5","11.158.105.153:20100","jiesi-7.5","9CCBDFB47A07D620");
        String indexName = "flow_interface_manage";

        DeleteIndexRequest deleteRequest = new DeleteIndexRequest();
        deleteRequest.indices(indexName);
        AcknowledgedResponse deleteResponse = client.admin().indices().delete(deleteRequest).actionGet();
        if(deleteResponse.isAcknowledged()){
            log.info("索引已删除："+deleteResponse.isAcknowledged());
        }

        CreateIndexRequest request = new CreateIndexRequest(indexName);
        request.settings(Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.include_type_name", false)
                .put("index.number_of_replicas", 1));
        request.source(source,XContentType.JSON);




         IndicesExistsResponse iRes = client.admin().indices().prepareExists(indexName)
                .execute().actionGet();
        if (iRes.isExists()) {
            //DeleteIndex.testDeleteIndex(indexName);
            log.info("索引已经存在" + indexName + ",不创建");
            return;
        }

        CreateIndexResponse response = client.admin().indices().create(request).actionGet();
        log.info("创建索引成功：" + response.isAcknowledged());


    }
    @Test
    public void testCreateIndex(){
        Client client = getClient();
        Map<String,Object> data = new HashMap<String,Object>();
        data.put("interfaceId",1L);
        data.put("name","测试数据");
        data.put("erps","wangjingfang3 tangqianqian11");
        data.put("serviceCode","test");
        data.put("appName","应用123");
        data.put("appCode","jdos-fdsf");
        data.put("deptName","京东零售");
        IndexResponse response = client.prepareIndex("flow_interface_manage", "_doc")
                .setSource(data).execute().actionGet();
        log.info("response_is:{}",response);
    }



    private static void deleteIndex(String indexName){
        client = getClient("jiesi-7.5","11.158.105.153:20100","jiesi-7.5","9CCBDFB47A07D620");

        DeleteIndexRequest deleteRequest = new DeleteIndexRequest();
        deleteRequest.indices(indexName);
        AcknowledgedResponse deleteResponse = client.admin().indices().delete(deleteRequest).actionGet();
        if(deleteResponse.isAcknowledged()){
            log.info("索引已删除："+deleteResponse.isAcknowledged());
        }
    }
    @Test
    public  void testDeleteInddex(){
       deleteIndex("flow_interface_manage");
        deleteIndex("flow_method_manage");
    }
    private static void createIndex(String indexName,String source){
        client = getClient("jiesi-7.5","11.158.105.153:20100","jiesi-7.5","9CCBDFB47A07D620");
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        request.settings(Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.include_type_name", false)
                .put("index.number_of_replicas", 1));
        request.source(source,XContentType.JSON);




        IndicesExistsResponse iRes = client.admin().indices().prepareExists(indexName)
                .execute().actionGet();
        if (iRes.isExists()) {
            //DeleteIndex.testDeleteIndex(indexName);
            log.info("索引已经存在" + indexName + ",不创建");
            return;
        }

        CreateIndexResponse response = client.admin().indices().create(request).actionGet();
        log.info("创建索引成功：" + response.isAcknowledged());
    }
    @Test
    public  void testCreateInterfaceAndMethod(){
        String interfaceJson = "{\n" +
                "  \"settings\": {\n" +
                "    \"number_of_shards\": 1,\n" +
                "    \"number_of_replicas\": 1\n" +
                "  },\n" +
                "  \"mappings\":{\n" +
                "    \"_doc\": {\n" +
                "      \"properties\":{\n" +
                "        \"interfaceId\":{\n" +
                "          \"type\":\"long\"\n" +
                "        },\n" +
                "        \"name\":{\n" +
                "          \"type\": \"text\",\n" +
                "          \"analyzer\": \"ik_max_word\"\n" +
                "        },\n" +
                "        \"erps\": {\n" +
                "          \"type\": \"text\",\n" +
                "          \"analyzer\": \"whitespace\"\n" +
                "        },\n" +
                "        \"serviceCode\": {\n" +
                "          \"type\": \"text\",\n" +
                "          \"analyzer\": \"ik_max_word\"\n" +
                "        },\n" +
                "        \"appName\": {\n" +
                "          \"type\": \"text\",\n" +
                "          \"analyzer\": \"ik_max_word\"\n" +
                "        },\n" +
                "        \"appCode\": {\n" +
                "          \"type\": \"text\",\n" +
                "          \"analyzer\": \"ik_max_word\"\n" +
                "        },\n" +
                "        \"deptName\": {\n" +
                "          \"type\": \"text\",\n" +
                "          \"analyzer\": \"ik_max_word\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
        String methodJson = "{\n" +
                "  \"settings\": {\n" +
                "    \"number_of_shards\": 1,\n" +
                "    \"number_of_replicas\": 1\n" +
                "  },\n" +
                "  \"mappings\": {\n" +
                "    \"_doc\": {\n" +
                "      \"properties\": {\n" +
                "        \"methodId\": {\n" +
                "          \"type\": \"long\"\n" +
                "        },\n" +
                "        \"name\": {\n" +
                "          \"type\": \"text\",\n" +
                "          \"analyzer\": \"ik_max_word\"\n" +
                "        },\n" +
                "        \"type\": {\n" +
                "          \"type\": \"keyword\"\n" +
                "        },\n" +
                "        \"methodCode\": {\n" +
                "          \"type\": \"text\",\n" +
                "          \"analyzer\": \"ik_max_word\"\n" +
                "        },\n" +
                "        \"docInfo\": {\n" +
                "          \"type\": \"text\",\n" +
                "          \"analyzer\": \"ik_max_word\"\n" +
                "        },\n" +
                "        \"interfaceId\": {\n" +
                "          \"type\": \"long\"\n" +
                "        },\n" +
                "        \"content\": {\n" +
                "          \"type\": \"text\",\n" +
                "          \"analyzer\": \"ik_max_word\"\n" +
                "        },\n" +
                "        \"path\": {\n" +
                "          \"type\": \"text\",\n" +
                "          \"analyzer\": \"ik_max_word\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
        createIndex("flow_interface_manage",interfaceJson);
        createIndex("flow_method_manage",methodJson);

    }
}
