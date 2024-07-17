package com.jd.cjg.client;


import com.jd.binlog.client.WaveEntry.EventType;
import com.jd.jmq.common.message.Message;
import java.util.List;
import java.util.Map;

public interface ConvertBean<S> {
    void dealMessage(List<Message> msgs) throws Exception;

    void dealBusinessData(Map<String, List<S>> data, EventType eventType,List<S> updated,List<S> removed);

    void insertEntity(List<S> before, List<S> after);

    void deleteEntity(List<S> before, List<S> after);

    void updateEntity(List<S> before, List<S> after);
    void processAll(List<S> updated,List<S> removed);
}