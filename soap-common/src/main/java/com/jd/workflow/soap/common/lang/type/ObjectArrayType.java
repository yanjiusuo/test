package com.jd.workflow.soap.common.lang.type;


import java.util.Collection;

public class ObjectArrayType extends DefaultObjectType {
    private static final long serialVersionUID = -9127560638647712015L;

    ObjectArrayType(Class type) {
        super(type);
    }

    protected Object doConvert(Object value) {
        if (value instanceof Object[]) {
            return (Object[])((Object[]) value);
        } else {


            return value instanceof Collection ? ((Collection) value).toArray() : null;
        }
    }
}
