package com.jd.workflow.metrics.alarm.granafa.api;

import com.jd.workflow.metrics.alarm.granafa.api.dto.*;
import com.jd.workflow.soap.common.util.JsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * granafa 管理数据源，比如 prometheus
 ： https://grafana.com/docs/grafana/latest/developers/http_api/data_source/
 */
public class DataSourceApi extends BaseApi{


    public DataSourceResponse create(CreateDataSourceInfo dataSourceInfo){
        String result = client.post("/api/datasources", null, dataSourceInfo);
        return JsonUtils.parse(result,DataSourceResponse.class);
    }
    public List<DataSourceResponse> list(){
        String result = client.get("/api/datasources", null);
        return JsonUtils.parseArray(result,DataSourceResponse.class);
    }
    public DataSourceResponse getById(String id){
        String result = client.get("/api/datasources/"+id, null);
        return JsonUtils.parse(result,DataSourceResponse.class);
    }
    public DataSourceResponse getByUid(String id){
        String result = client.get("/api/datasources/uid/"+id, null);
        return JsonUtils.parse(result,DataSourceResponse.class);
    }
    public DataSourceResponse getByName(String name){
        String result = client.get("/api/datasources/name/"+name, null);
        return JsonUtils.parse(result,DataSourceResponse.class);
    }

    public DataSourceHealthCheckResponse checkHealthById(String dataSourceId){

        String result = client.get("/api/datasources/"+dataSourceId+"/health", null);
        return JsonUtils.parse(result,DataSourceHealthCheckResponse.class);
    }
    public DataSourceHealthCheckResponse checkHealthByUid(String uid){

        String result = client.get("/api/datasources/uid/"+uid+"/health", null);
        return JsonUtils.parse(result,DataSourceHealthCheckResponse.class);
    }

}
