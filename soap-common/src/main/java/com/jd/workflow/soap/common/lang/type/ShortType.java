package com.jd.workflow.soap.common.lang.type;



public class ShortType extends DefaultObjectType {

    ShortType(Class type) {
        super(type);
    }

    protected Object doConvert(Object value) {
        return ObjectTypes.convertToShort(value);
    }
}
