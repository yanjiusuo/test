package com.jd.workflow.console.service;

import com.jd.workflow.console.dto.PublishRecordDto;

/**
 * 用来发布route记录,可以根据需要发布到数据库或者ducc
 */
public interface IRoutePublishService {
    public void publish(PublishRecordDto dto);
}
