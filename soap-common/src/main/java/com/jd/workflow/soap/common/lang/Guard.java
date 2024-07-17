package com.jd.workflow.soap.common.lang;


import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.ObjectHelper;

public class Guard {
    public Guard() {
    }

    public static void assertTrue(boolean value) {
        assertTrue(value, "lang.err_assert_fail");
    }

    public static void assertTrue(boolean value, String errorMsg, Object... args) {
        if (!value) {
            throw (new BizException(errorMsg));
        }
    }

    public static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new BizException(message);
        }
    }
    public static void assertTrue(boolean value,String message, int code) {
        if (!value) {
            throw new BizException(message).code(code);
        }
    }
    public static void assertEquals(Object obj1, Object obj2) {
        if (!ObjectHelper.equals(obj1, obj2)) {
            throw (new BizException("lang.err_value_not_equals")).param("expected", obj1).param("actual", obj2);
        }
    }

    public static <T> T notNull(T value, String message, Object... args) {
        if (value == null) {
            throw (new BizException(message)).param("value", value);
        } else {
            return value;
        }
    }

    public static <T> T notNull(T value, String msg) {
        if (value == null) {
            throw (new BizException(msg)).param("value", value);
        } else {
            return value;
        }
    }

   /* public static <T> T notEmpty(T value, String msg, Object... args) {
        if (ObjectHelper.isEmpty(value)) {
            throw (new BizException(msg)).param("value", value).args(args);
        } else {
            return value;
        }
    }*/
    public static <T> T notEmpty(T value, String msg) {
       return notEmpty(value,msg,null);
    }
    public static <T> T notEmpty(T value, String msg,Integer code) {
        return notEmpty(value,msg,code,null);
    }
    public static <T> T notEmpty(T value, String msg,Integer code,Object data) {
        if (ObjectHelper.isEmpty(value)) {
            throw (new BizException(msg)).code(code).data(data).param("value", value);
        } else {
            return value;
        }
    }
    public static int nonNegativeInt(int value, String msg, Object... args) {
        if (value < 0) {
            throw (new BizException(msg)).param("value", value);
        } else {
            return value;
        }
    }

    public static long nonNegativeLong(long value, String msg, Object... args) {
        if (value < 0L) {
            throw (new BizException(msg)).param("value", value);
        } else {
            return value;
        }
    }
}
