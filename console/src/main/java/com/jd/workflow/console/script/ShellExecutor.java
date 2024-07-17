package com.jd.workflow.console.script;

import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jd.workflow.console.utils.TemplateUtils;
import com.jd.workflow.soap.common.util.JsonUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ShellExecutor implements ScriptExecutor {


    /**
     *
     */
    private final ExecutorService executorService;

    /**
     * 
     */
    public ShellExecutor() {
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public Future<ExecutionResult> executeAsync(String key,String scriptContent, Map<String, String> parameters) {
        return executorService.submit(() -> {
            try {
                // 将参数替换到脚本内容中
                String tpl;
                if(CollectionUtil.isNotEmpty(parameters)){
                    tpl = TemplateUtils.renderTpl(scriptContent, parameters);
                }else {
                    tpl = scriptContent;
                }

                Process process = Runtime.getRuntime().exec("/bin/bash -c " + tpl);
                StringBuilder outputBuilder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        outputBuilder.append(line).append("\n");
                    }
                }
                // 返回执行结果
                return new ExecutionResult(key,outputBuilder.toString(), null);
            } catch (Exception e) {
                return new ExecutionResult(key,null, e);
            }
        });
    }

    //    @Override
    public String execute(String scriptContent, Map<String, String> parameters) {
        // 将参数替换到脚本内容中
        String tpl;
        if(CollectionUtil.isNotEmpty(parameters)){
            tpl = TemplateUtils.renderTpl(scriptContent, parameters);
        }else {
            tpl = scriptContent;
        }
        // 使用ProcessBuilder执行shell命令
        try {
            StringBuilder outputBuilder = new StringBuilder();
            ProcessBuilder pb =  new ProcessBuilder("/bin/bash", "-c",tpl);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                outputBuilder.append(line);
            }
            reader.close();
            int exitCode = process.waitFor();
            System.out.println("Exited with error code : " + exitCode);
            return outputBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 测试
     * @param args
     */
    public static void main(String[] args) {
//        String scriptStr="curl 'https://api.apifox.com/api/v1/user?locale=zh-CN' \\\\-H 'accept: */*' \\\\-H 'accept-language: zh-CN' \\\\-H 'access-control-allow-origin: *' \\\\-H 'authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6NDU0ODA0LCJ0cyI6Ijk0NzhiZmZiZmMwOGE5OWUiLCJpYXQiOjE2OTgzMDI4NjA2MDF9._gJfbkhS3S0h1Fm_06u3aljnxWnyc1jPz_DdwxUQ5ZA' \\\\-H 'origin: https://app.apifox.com' \\\\-H 'priority: u=1, i' \\\\-H 'referer: https://app.apifox.com/' \\\\-H 'sec-ch-ua: \\\"Chromium\\\";v=\\\"124\\\", \\\"Google Chrome\\\";v=\\\"124\\\", \\\"Not-A.Brand\\\";v=\\\"99\\\"' \\\\-H 'sec-ch-ua-mobile: ?0' \\\\-H 'sec-ch-ua-platform: \\\"macOS\\\"' \\\\-H 'sec-fetch-dest: empty' \\\\-H 'sec-fetch-mode: cors' \\\\-H 'sec-fetch-site: same-site' \\\\-H 'user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36' \\\\-H 'x-branch-id: 0' \\\\-H 'x-client-mode: web' \\\\-H 'x-client-version: 2.5.25-alpha.2' \\\\-H 'x-device-id: oZzBHSnx-HywS-Uj37-j2fv-La7NVI4dzeYm' \\\\-H 'x-project-id;'";
        String scriptStr="curl 'https://api.apifox.com/api/v1/user?locale=zh-CN' \\\n" +
                "  -H 'accept: */*' \\\n" +
                "  -H 'accept-language: zh-CN' \\\n" +
                "  -H 'access-control-allow-origin: *' \\\n" +
                "  -H 'authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6NDU0ODA0LCJ0cyI6Ijk0NzhiZmZiZmMwOGE5OWUiLCJpYXQiOjE2OTgzMDI4NjA2MDF9._gJfbkhS3S0h1Fm_06u3aljnxWnyc1jPz_DdwxUQ5ZA' \\\n" +
                "  -H 'origin: https://app.apifox.com' \\\n" +
                "  -H 'priority: u=1, i' \\\n" +
                "  -H 'referer: https://app.apifox.com/' \\\n" +
                "  -H 'sec-ch-ua: \"Chromium\";v=\"124\", \"Google Chrome\";v=\"124\", \"Not-A.Brand\";v=\"99\"' \\\n" +
                "  -H 'sec-ch-ua-mobile: ?0' \\\n" +
                "  -H 'sec-ch-ua-platform: \"macOS\"' \\\n" +
                "  -H 'sec-fetch-dest: empty' \\\n" +
                "  -H 'sec-fetch-mode: cors' \\\n" +
                "  -H 'sec-fetch-site: same-site' \\\n" +
                "  -H 'user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36' \\\n" +
                "  -H 'x-branch-id: 0' \\\n" +
                "  -H 'x-client-mode: web' \\\n" +
                "  -H 'x-client-version: 2.5.25-alpha.2' \\\n" +
                "  -H 'x-device-id: oZzBHSnx-HywS-Uj37-j2fv-La7NVI4dzeYm' \\\n" +
                "  -H 'x-project-id;'";
//        String result = execute22(scriptStr, null);
//        ScriptResponse<Object> scriptResponse = JsonUtils.parse(result, new TypeReference<ScriptResponse<Object>>() {});
//        System.out.println(result);

    }
}
