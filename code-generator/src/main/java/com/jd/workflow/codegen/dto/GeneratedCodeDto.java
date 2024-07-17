package com.jd.workflow.codegen.dto;

import com.jd.workflow.codegen.FileCode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class GeneratedCodeDto {
    String mainCode;
    List<FileCode> fileCodes = new ArrayList<>();

}
