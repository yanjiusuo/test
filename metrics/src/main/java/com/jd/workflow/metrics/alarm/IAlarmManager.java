package com.jd.workflow.metrics.alarm;

public interface IAlarmManager {
    public void addAlarmRule(AlarmRule alarmRule);
    public void updateAlarmRule(AlarmRule alarmRule);
    public void removeAlarmRule(AlarmRule alarmRule);
    public AlarmList getAlarmList();
}
