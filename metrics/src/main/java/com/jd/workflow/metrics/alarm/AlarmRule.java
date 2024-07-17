package com.jd.workflow.metrics.alarm;

import lombok.Data;

@Data
public class AlarmRule {
    String interfaceName;
    String metric;
    FailAlarmRule failAlarmRule;
    CallCounterRule callCounterRule;
    TpAlarmRule tpAlarmRule;
    AlarmNotifyInfo notifyInfo;

    // 失败比例
    @Data
    public static class FailAlarmRule extends BaseAlarmRule {
        CounterAlarmType alarmType;
        AlarmLevel level;
        Integer size;
        Integer total;
    }

    // 调用量告警
    @Data
    public static class CallCounterRule extends BaseAlarmRule {


        Integer size;
    }

    // 告警
    @Data
    public static class TpAlarmRule extends BaseAlarmRule {


        Double max;
        Double avg;

        Double tp90;
        Double tp50;
        Double tp99;
        Double tp999;

        public boolean isEmpty() {
            return max != null
                    && avg != null
                    && tp90 != null
                    && tp50 != null
                    && tp99 != null
                    && tp999 != null
                    ;
        }
    }

    @Data
    public static class BaseAlarmRule {
        CounterAlarmType alarmType;
        int matchCount;
        AlarmLevel alarmLevel;
        // 10分钟内不重复报警
        int alarmIntervalSec = 5 * 60;
    }

    public static enum CounterAlarmType {
        RATE, COUNT
    }

}
