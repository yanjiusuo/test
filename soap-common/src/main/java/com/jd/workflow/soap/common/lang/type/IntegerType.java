package com.jd.workflow.soap.common.lang.type;




public class IntegerType extends DefaultObjectType {
    private static final long serialVersionUID = -9127560638647712015L;

    IntegerType(Class clazz) {
        super(clazz);
    }

    protected Object doConvert(Object value) {
        return ObjectTypes.convertToInteger(value);
    }
}