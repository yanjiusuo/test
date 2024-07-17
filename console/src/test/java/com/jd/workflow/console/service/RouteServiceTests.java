package com.jd.workflow.console.service;

import com.jd.workflow.console.entity.RouteConfigRecord;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class RouteServiceTests extends Assert {
    RouteService routeService = new RouteService();

    RouteConfigRecord newRecord(long id){
        RouteConfigRecord record = new RouteConfigRecord();
        record.setId(id);

        return record;
    }
    RouteConfigRecord newRecord(long id,Long version){
        RouteConfigRecord record = new RouteConfigRecord();
        record.setId(id);
        record.setVersion(version);
        return record;
    }
    @Test
    public void routeCollectNew(){
        List<RouteConfigRecord> records = new ArrayList();
        RouteConfigRecord record = newRecord(1L);
        record.setMethodId("1");
        records.add(record);
        List<Long> addOrUpdateIds = new ArrayList<>();
        List<String> removed = new ArrayList<>();
        routeService.collectChangeRoutes(records,addOrUpdateIds,removed);
        assertEquals(1,records.size());
        assertEquals(1L,(long)records.get(0).getId());

    }
    RouteService.RouteItem newRouteItem(Long versionId){
        RouteService.RouteItem routeItem = new RouteService.RouteItem();
        routeItem.setVersionId(versionId);
        return routeItem;
    }
    @Test
    public void routeCollectRemoved(){
        routeService.routes.put("1",newRouteItem(1L));

        List<RouteConfigRecord> records = new ArrayList();

        List<Long> addOrUpdateIds = new ArrayList<>();
        List<String> removed = new ArrayList<>();
        routeService.collectChangeRoutes(records,addOrUpdateIds,removed);
        assertEquals(1,removed.size());
        assertEquals("1",(String)removed.get(0));
        routeService.routes.remove(1L);
    }
    @Test
    public void routeCollectUpdated(){
        routeService.routes.put("1",newRouteItem(1L));

        List<RouteConfigRecord> records = new ArrayList();
        RouteConfigRecord record = newRecord(1L, 2L);
        record.setMethodId("1");
        records.add(record);


        List<Long> addOrUpdateIds = new ArrayList<>();
        List<String> removed = new ArrayList<>();
        routeService.collectChangeRoutes(records,addOrUpdateIds,removed);
        assertEquals(1,records.size());
        assertEquals(1L,(long)records.get(0).getId());

        routeService.routes.remove(1L);

    }
}
