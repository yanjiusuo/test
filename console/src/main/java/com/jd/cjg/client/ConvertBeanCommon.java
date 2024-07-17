package com.jd.cjg.client;


import com.alibaba.fastjson.JSONObject;
import com.jd.binlog.client.EntryMessage;
import com.jd.binlog.client.MessageDeserialize;
import com.jd.binlog.client.WaveEntry.Column;
import com.jd.binlog.client.WaveEntry.EntryType;
import com.jd.binlog.client.WaveEntry.EventType;
import com.jd.binlog.client.WaveEntry.Header;
import com.jd.binlog.client.WaveEntry.RowData;
import com.jd.binlog.client.impl.JMQMessageDeserialize;
import com.jd.jmq.common.message.Message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;

public abstract class ConvertBeanCommon<S> implements ConvertBean<S> {
    private MessageDeserialize deserialize = new JMQMessageDeserialize();

    public ConvertBeanCommon() {
    }

    public void dealMessage(List<Message> messages) throws Exception {
        List<EntryMessage> entryMessages = this.deserialize.deserialize(messages);
        if (messages != null && !messages.isEmpty()) {
            List<S> updated = new ArrayList<>();
            List<S> removed = new ArrayList<>();
            Iterator entryMsgIter = entryMessages.iterator();

            while(true) {
                EntryMessage entryMessage;
                do {
                    do {
                        if (!entryMsgIter.hasNext()) {
                            this.processAll(updated,removed);
                            return;
                        }

                        entryMessage = (EntryMessage)entryMsgIter.next();
                    } while(entryMessage.getEntryType().equals(EntryType.TRANSACTIONBEGIN));
                } while(entryMessage.getEntryType().equals(EntryType.TRANSACTIONEND));

                TableClzz tableClzz = (TableClzz)AnnotationUtils.findAnnotation(this.getClass(), TableClzz.class);
                String tablename = tableClzz.tablename();
                Class clzz = tableClzz.clzz();
                if (StringUtils.isEmpty(tablename) || Objects.isNull(clzz)) {
                    return;
                }

                List<RowData> rowDatas = entryMessage.getRowChange().getRowDatasList();
                Header header = entryMessage.getHeader();
                String logTableName = header.getTableName();
                if (tablename.equals(logTableName)) {
                    Map<String, List<S>> map = afterDOS(rowDatas, clzz);
                    this.dealBusinessData(map, entryMessage.getHeader().getEventType(),updated,removed);
                }
            }

        }
    }

    public void dealBusinessData(Map<String, List<S>> map, EventType type,List<S> updated,List<S> removed) {
        List<S> after = (List)map.get("after");
        List before = (List)map.get("before");

        try {
            if (type == EventType.INSERT) {
                this.insertEntity((List)null, after);
                updated.addAll(after);
            } else if (type == EventType.DELETE) {
                this.deleteEntity(before, (List)null);
                removed.addAll(before);
            } else if (type == EventType.UPDATE) {
                this.updateEntity(before, after);
                updated.addAll(after);
            }

        } catch (Exception var6) {
            throw var6;
        }
    }

    public static <T> Map<String, List<T>> afterDOS(List<RowData> rowData, Class<T> clazz) {
        if (CollectionUtils.isEmpty(rowData)) {
            return Collections.emptyMap();
        } else {
            List<T> result = new ArrayList();
            List<T> beforeresult = new ArrayList();
            rowData.forEach((row) -> {
                Map<String, String> afterMap = new HashMap();
                if (CollectionUtils.isNotEmpty(row.getAfterColumnsList())) {
                    Iterator var5 = row.getAfterColumnsList().iterator();

                    while(var5.hasNext()) {
                        Column afterRow = (Column)var5.next();
                        String fieldName = changeToJavaFiled(afterRow.getName());
                        afterMap.put(fieldName, afterRow.getValue());
                    }

                    String jsonx = JSONObject.toJSONString(afterMap);
                    T object = JSONObject.parseObject(jsonx, clazz);
                    result.add(object);
                }

                Map<String, String> beforeMap = new HashMap();
                if (CollectionUtils.isNotEmpty(row.getBeforeColumnsList())) {
                    Iterator var12 = row.getBeforeColumnsList().iterator();

                    while(var12.hasNext()) {
                        Column beforeRow = (Column)var12.next();
                        String fieldNamex = changeToJavaFiled(beforeRow.getName());
                        beforeMap.put(fieldNamex, beforeRow.getValue());
                    }

                    String json = JSONObject.toJSONString(beforeMap);
                    T objectx = JSONObject.parseObject(json, clazz);
                    beforeresult.add(objectx);
                }

            });
            Map<String, List<T>> map = new HashMap();
            map.put("before", beforeresult);
            map.put("after", result);
            return map;
        }
    }

    public static String changeToJavaFiled(String field) {
        String[] fields = field.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder(fields[0]);

        for(int i = 1; i < fields.length; ++i) {
            char[] cs = fields[i].toCharArray();
            cs[0] = (char)(cs[0] - 32);
            sb.append(String.valueOf(cs));
        }

        return sb.toString();
    }
}