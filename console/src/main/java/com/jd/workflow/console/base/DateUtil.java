package com.jd.workflow.console.base;

import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 项目名称：联调平台控制台
 * 类 名 称：MD5Util
 * 类 描 述：时间工具类
 * 创建时间：2022-11-08 14:13
 * 创 建 人：wangxiaofei8
 */
@Slf4j
public class DateUtil {
    static SimpleDateFormat DATE_FORMAT  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取当前时间、可做版本号
     * @return
     */
    public static String getCurrentDateMillTime() {
        Calendar calendar = Calendar.getInstance();
        return getCurrentDateMillTime(calendar);
    }

    public static String getCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        StringBuilder builder = new StringBuilder();
        builder.append(calendar.get(Calendar.YEAR));
        fillZero(builder,1,calendar.get(Calendar.MONTH) + 1);
        fillZero(builder,1,calendar.get(Calendar.DATE));
        return builder.toString();
    }

    public static Date parseDate(String dateStr) {
        try {
            return DATE_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            throw StdException.adapt(e);
        }
    }
    /**
     * 毫秒时间字符串 yyyyMMddhhmmssSSS
     * @param calendar
     * @return
     */
    public static String getCurrentDateMillTime(final Calendar calendar) {
        StringBuilder builder = new StringBuilder();
        builder.append(calendar.get(Calendar.YEAR));
        fillZero(builder,1,calendar.get(Calendar.MONTH) + 1);
        fillZero(builder,1,calendar.get(Calendar.DATE));
        fillZero(builder,1,calendar.get(Calendar.HOUR_OF_DAY));
        fillZero(builder,1,calendar.get(Calendar.MINUTE));
        fillZero(builder,1,calendar.get(Calendar.SECOND));
        fillZero(builder,2,calendar.get(Calendar.MILLISECOND));
        return builder.toString();
    }

    /**
     * 补位对齐
     * @param builder
     * @param count
     * @param appendNum
     */
    private static void fillZero(StringBuilder builder,int count,int appendNum){
        while (count>0){
            int i = Double.valueOf(Math.pow(10, count)).intValue();
            if(appendNum<i){
                builder.append(0);
            }else{
                break;
            }
            count--;
        }
        builder.append(appendNum);
    }

    /**
     * 获取几天前日期字符串 格式  yyyy-MM-dd HH:mm:ss
     * @param previousDayNo
     * @return
     */
    public static String obtainPreviousDateStr(int previousDayNo ){
        LocalDateTime now = LocalDate.now().atStartOfDay();
        LocalDateTime previousDay = now.minusDays(previousDayNo);
        return df.format(previousDay);
    }

    public static Date getFirstDateOfYear(){
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate localDate = LocalDate.now();
        LocalDate firstDateOfYear = localDate.withDayOfYear(1);
        ZonedDateTime zdt = firstDateOfYear.atStartOfDay(zoneId);
        return Date.from(zdt.toInstant());
    }

    /**
     * 获取指定时间字符串季度 数
     * @param dateStr
     * @return
     */
    public static int getQuarterOfDay(String dateStr) {
        Date date = null;
        try {
            date = parseDate(dateStr);
        } catch (Exception e) {
            log.error("转换日期失败，", e);
            return 0;
        }
        return getQuarterNo(date);
    }

    /**
     * 获取日期的季度
     * @param date 日期
     * @return
     */
    public static int getQuarterNo(Date date) {
        int month = 0;
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        //由于month从0开始，所有这里+1
        month = c.get(Calendar.MONTH) + 1;
        return month % 3 == 0 ? month / 3 : month / 3 + 1;
    }

    public static void main(String[] args) {
        Date firstDateOfYear = getFirstDateOfYear();
        String str = DATE_FORMAT.format(firstDateOfYear);

        int z = 0;
        for(int i=0 ; i<100 ; i++){
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(getCurrentDate());
            System.out.println(getCurrentDateMillTime());
        }


    }
}
