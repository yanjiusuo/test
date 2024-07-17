package com.jd.workflow.console.dto.remote;

import lombok.Data;

import java.util.List;

@Data
public class CjgPage<T> {
    List<T> content;
    List<T> data;
    long totalPages;
    long totalElements;
    long size;
    long numberOfElements;
}
