package com.jd.workflow.console.fps.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author wufagang
 * @description
 * @date 2023年04月21日 15:44
 */
@Data
public class DataType {

    private String url;

    private MultipartFile file;
}
