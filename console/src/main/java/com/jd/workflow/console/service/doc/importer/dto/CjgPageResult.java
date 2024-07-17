package com.jd.workflow.console.service.doc.importer.dto;

import lombok.Data;

import java.util.List;

@Data
public class CjgPageResult<T> {
    int pages;
    int current;
    T records;
}
