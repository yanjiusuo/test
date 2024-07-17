package com.jd.workflow.console.service.impl;

import com.jd.workflow.console.dto.PublishRecordDto;
import com.jd.workflow.console.service.IRoutePublishService;
import com.jd.workflow.console.service.RouteConfigRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value="route.useDuccPublisher",havingValue = "false")
public class DbRoutePublishServiceImpl implements IRoutePublishService {
    @Autowired
    RouteConfigRecordService routeConfigRecordService;
    @Override
    public void publish(PublishRecordDto dto) {
        routeConfigRecordService.publish(dto);
    }
}
