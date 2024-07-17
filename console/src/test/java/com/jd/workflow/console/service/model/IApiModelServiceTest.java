package com.jd.workflow.console.service.model;


import com.jd.workflow.BaseTestCase;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import junit.framework.TestCase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/4
 */
public class IApiModelServiceTest extends BaseTestCase {


    @Autowired
    private IApiModelService apiModelService;
    @Test
    public void testParseSql() {
        String sql = "CREATE TABLE `my_table` (\n" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                "  `name` varchar(50) NOT NULL COMMENT 'User name',\n" +
                "  `age` int(11) DEFAULT NULL COMMENT 'User age',\n" +
                "  `salary` decimal(10,2) DEFAULT NULL COMMENT 'User salary',\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='My Table';";

        ObjectJsonType tableObj = apiModelService.parseSql(sql);

//        // Assert table name
//        assertEquals("my_table", tableObj.getName());
//
//        // Assert table description
//        assertEquals("My Table", tableObj.getDesc());
//
//        // Assert column count
//        assertEquals(4, tableObj.getChildren().size());
//
//        // Assert column types
//        assertEquals("integer", tableObj.getChildren().get(0).getType());
//        assertEquals("long", tableObj.getChildren().get(1).getType());
//        assertEquals("integer", tableObj.getChildren().get(2).getType());
//        assertEquals("double", tableObj.getChildren().get(3).getType());
//
//        // Assert column names and descriptions
//        assertEquals("id", tableObj.getChildren().get(0).getName());
//        assertEquals("", tableObj.getChildren().get(0).getDescription());
//        assertEquals("name", tableObj.getChildren().get(1).getName());
//        assertEquals("User name", tableObj.getChildren().get(1).getDescription());
//        assertEquals("age", tableObj.getChildren().get(2).getName());
//        assertEquals("User age", tableObj.getChildren().get(2).getDescription());
//        assertEquals("salary", tableObj.getChildren().get(3).getName());
//        assertEquals("User salary", tableObj.getChildren().get(3).getDescription());
    }
}