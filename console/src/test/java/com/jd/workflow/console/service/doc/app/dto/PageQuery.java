package com.jd.workflow.console.service.doc.app.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PageQuery {
    long pageNo;
    long pageSize;
    Person person;
    Map<String,Object> map;
    List<Person> list;
}
