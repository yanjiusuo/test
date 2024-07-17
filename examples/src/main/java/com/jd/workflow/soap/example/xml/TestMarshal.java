package com.jd.workflow.soap.example.xml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class TestMarshal {
    public static void marshal(){
        Data data = new Data();
        List<User> users = new ArrayList<>();
        users.add(new User("id1","name1"));
        users.add(new User("id1","name1"));
        users.add(new User("id1","name1"));
        data.setUser(users);
        StringWriter writer = new StringWriter(); // XML output here
        JAXB.marshal(data, writer); // write Equation to StringWriter
        System.out.println(writer.toString());  // return XML string
    }
    public static void unarshal() throws IOException {
        String xml = "<data><id>1</id><id>2</id></data>";
        //Map result = JAXB.unmarshal(xml, Map.class);
        //System.out.println(JSON.toJSONString(result));
        XmlMapper xmlMapper = new XmlMapper();
        JsonNode jsonNode = xmlMapper.readTree(xml.getBytes());

        System.out.println(jsonNode.get("name").asText("无名"));
        System.out.println(jsonNode.get("age").asInt(99));
        System.out.println(jsonNode.get("title"));

    }
    public static void main(String[] args) throws IOException {
        unarshal();
        // marshal();
    }
    @lombok.Data
    static class Data{
        List<User> user;
    }
    @lombok.Data
    static class User{
        String id;
        String name;
        public User(String id,String name){
            this.id = id;
            this.name = name;
        }
    }
}
