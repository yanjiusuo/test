package com.jd.workflow.metrics.alarm.granafa.api;

import com.jd.workflow.metrics.alarm.granafa.api.dto.Folder;
import com.jd.workflow.metrics.alarm.granafa.api.dto.FolderResponse;
import com.jd.workflow.metrics.client.GrafanaClient;
import com.jd.workflow.soap.common.util.JsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * granafa文件按管理api
 ：https://grafana.com/docs/grafana/latest/developers/http_api/folder/
 */
public class FolderApi extends BaseApi{

    public FolderResponse updateFolder(Folder folder){
        String result = client.put("/api/folders/"+folder.getUid(), null, folder);
        return JsonUtils.parse(result,FolderResponse.class);
    }
    public FolderResponse createFolder(Folder folder){
        String result = client.post("/api/folders", null, folder);
        return JsonUtils.parse(result,FolderResponse.class);
    }
    public FolderResponse getFolderByUid(String uid){
        String result = client.get("/api/folders/"+uid, null);
        return JsonUtils.parse(result,FolderResponse.class);
    }
    public List<FolderResponse> listFolder(Integer limit){
        if(limit == null){
            limit = 10;
        }
        Map<String,Object> params = new HashMap<>();
        params.put("limit",limit);
        String result = client.get("/api/folders", params);
        return JsonUtils.parseArray(result,FolderResponse.class);
    }
    public FolderResponse deleteFolderByUid(String uid){
        String result = client.delete("/api/folders/"+uid);
        return JsonUtils.parse(result,FolderResponse.class);
    }
}
