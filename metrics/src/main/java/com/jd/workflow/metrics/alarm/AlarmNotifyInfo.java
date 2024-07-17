package com.jd.workflow.metrics.alarm;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AlarmNotifyInfo {
     List<String> userName= new ArrayList<>();// 用户名
     List<String> email= new ArrayList<>(); // 邮件
     List<String> timline= new ArrayList<>();// 咚咚
     List<String> voiceCall= new ArrayList<>();// 语音
}

