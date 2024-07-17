package com.jd.workflow.console.dto.app;

import com.jd.workflow.soap.common.method.ClassMetadata;
import lombok.Data;

import java.util.List;

@Data
public class AppAllInfo {
    private Long appId;
    List<HttpMethodInfo> httpApis;
    List<EnvInfo> envInfos;
    List<ClassMetadata> jsfInterfaces;
    private String localEnvName;
    private String localEnvAddress;
    @Data
    public static class EnvInfo{
        private String url;
        private String id;

    }
    @Data
    public static class HttpMethodInfo{
        private Long id;

        /**
         * jsf方法名
         */
        private String methodCode;
        /**
         * jsf方法中文名
         */
        private String name;
        /**
         * http方法
         */
        private String httpMethod;
        /**
         * http路径
         */
        private String path;
    }
}
