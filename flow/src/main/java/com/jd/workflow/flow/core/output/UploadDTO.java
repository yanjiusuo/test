package com.jd.workflow.flow.core.output;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/23
 */

import lombok.Data;

import java.io.InputStream;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/23
 */
@Data
public class UploadDTO {


    private InputStream inputStream;

    private String fieldName;

    private String downloadUrl;
}
