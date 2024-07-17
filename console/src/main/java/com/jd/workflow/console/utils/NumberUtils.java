package com.jd.workflow.console.utils;


import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * @author： bjhaoyanjun
 * @Date: 12-4-19
 * Time: 上午11:06
 * To change this template use File | Settings | File Templates.
 * @date 2018-12-6 14:48:08
 * @author wanghongyun1
 */
@Slf4j
public class NumberUtils {

    /**
     * 时间格式
     */
    public static String FORMAT_STR = "yyyy-MM-dd HH:mm:ss";

    /**
     * 时间格式
     */
    public static String FORMAT_STR2 = "yyyy/MM/dd HH:mm:ss";

    /**
     * 转为字符串
     * @param o
     * @return
     */
    public static String toString(Object o) {
        String s = "";
        if (o != null) {
            s = o.toString();
        }
        return s;
    }

    /**
     * 拼接字符串
     * @param a
     * @param b
     * @return
     */
    public static String append(Object a, Object b) {
        StringBuilder result = new StringBuilder();
        if (a != null) {
            result.append(a.toString());
        }
        if (b != null) {
            result.append(b.toString());
        }
        return result.toString();
    }

    /**
     * 拼接字符串
     * @param a
     * @return
     */
    public static StringBuilder append(Object a) {
        StringBuilder result = new StringBuilder();
        if (a != null) {
            result.append(a.toString());
        }

        return result;
    }

    /**
     * 转化为int
     * @param o
     * @return
     */
    public static int toInt(Object o) {
        int i = 0;
        try {
            if (o != null) {
                i = Integer.parseInt(toString(o));
            }
        } catch (Exception e) {
            log.error( "#NumberUtil.toInt error",e);
        }
        return i;
    }

    /**
     * 转化为long
     * @param o
     * @return
     */
    public static long toLong(Object o) {
        long i = 0;
        try {
            if (o != null) {
                i = Long.parseLong(toString(o));
            }
        } catch (Exception e) {
            log.error( "#NumberUtil.toLong error",e);
        }
        return i;
    }

    /**
     * 转化为Double
     * @param o
     * @return
     */
    public static double toDouble(Object o) {
        double i = 0.0;
        try {
            if (o != null) {
                i = Double.parseDouble(toString(o));
            }
        } catch (Exception e) {
            log.error( "#NumberUtil.toDouble error",e);
        }
        return i;
    }

    /**
     * 转化为boolean
     * @param obj
     * @return
     */
    public static boolean toBoolean(Object obj) {
        try {
            return Boolean.parseBoolean(toString(obj));
        } catch (Exception e) {
            log.error( "#NumberUtil.toBoolean error",e);
            return false;
        }
    }
    /**
     * 转换成BigDecimal类型
     * @param obj
     * @return BigDecimal
     */
    public static BigDecimal toBigDecimal(Object obj){
        try {
            return new BigDecimal(toString(obj));
        } catch (Exception e) {
            log.error( "#NumberUtil.toBigDecimal error",e);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 随机数
     */
    private static Random random=new Random();

    /**
     * 获取下一个随机数
     * @param num
     * @return
     */
    public static int nextInt(int num){
        return random.nextInt(num);
    }

    /**
     * 是否为数字
     * @param input
     * @return
     */
    public static boolean isInteger(String input) {
        try {
            int result = Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            log.error( "#NumberUtil.isInteger error",e);
            return false;
        }
    }


    /**
      * 转换成String类型 注意当入参为空时返回结果为null，而不是空字符串
      * @author zhengmulan
      * @date 2018/12/27 14:52
      * @param o
      * @return String
      */
    public static String swapString(Object o){
        if (o != null && o instanceof String){
            return (String)o;
        }
        return null;
    }

    /**
     * 转换成List类型
     * @author zhengmulan
     * @date 2018/12/27 14:52
     * @param o
     * @return List
     */
    public static List swapList(Object o){
        if (o != null && o instanceof List){
            return (List)o;
        }
        return null;
    }

    /**
     * 转换成Map类型
     * @author zhengmulan
     * @date 2018/12/27 14:52
     * @param o
     * @return Map
     */
    public static Map swapMap(Object o){
        if (o != null && o instanceof Map){
            return (Map)o;
        }
        return null;
    }

    /**
     * 转换成BigDecimal类型
     * @param o
     * @return BigDecimal
     */
    public static BigDecimal swapBigDecimal(Object o){
        if (o != null && o instanceof BigDecimal){
            return (BigDecimal) o;
        }
        return null;
    }

    /**
     * 转换成Integer类型 注意当入参为空时返回结果为null使用时应做好判断避免空指针异常
     * @param o
     * @return Integer
     */
    public static Integer swapInteger(Object o){
        if (o != null && o instanceof Integer){
            return (Integer) o;
        }
        return null;
    }

    /**
     * 转换成Long类型 注意当入参为空时返回结果为null使用时应做好判断避免空指针异常
     * @param o
     * @return Long
     */
    public static Long swapLong(Object o){
        if (o != null && o instanceof Long){
            return (Long) o;
        }
        return null;
    }

    /**
     * 转换成Boolean类型 注意当入参为空时返回结果为null使用时应做好判断避免空指针异常
     * @param o
     * @return Boolean
     */
    public static Boolean swapBoolean(Object o){
        if (o != null && o instanceof Boolean){
            return (Boolean) o;
        }
        return null;
    }

    /**
     * 转化为日期
     * @param o
     * @return
     */
    public static Date toDate(Long o) {
        Date date = new Date();
        try {
            if (o == null || o.equals("")) {
                return new Date();
            }
            date = new Date(o);
        } catch (Exception e) {
            log.error( "#NumberUtil.toDate error",e);
        }
        return date;
    }

    /**
     * 格式化日期
     * @param o
     * @return
     */
    public static String toDateFormat(Date o, String formatStr) {
        String dateStr = "";
        try {
            if (o == null) {
                return null;
            }
            dateStr = new SimpleDateFormat(formatStr).format(o);
        } catch (Exception e) {
            log.error( "#NumberUtil.toDateFormat error",e);
        }
        return dateStr;
    }

}
