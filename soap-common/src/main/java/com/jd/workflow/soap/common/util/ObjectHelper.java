package com.jd.workflow.soap.common.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

public class ObjectHelper {
    public static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        } else if (value instanceof CharSequence) {
            return ((CharSequence)value).length() == 0;
        } else if (value instanceof Collection) {
            return ((Collection)value).isEmpty();
        } else if (value instanceof Map) {
            return ((Map)value).isEmpty();
        } else if (value.getClass().isArray()) {
            return Array.getLength(value) == 0;
        } else {
            return false;
        }
    }
    public static boolean equals(Object value1, Object value2) {
        return value1 == value2 || value1 != null && value1.equals(value2);
    }
}
