//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jd.workflow.soap.common.lang.type;


public interface IConverter {
    /**
     * 将value转换为type
     * @param value
     * @param type
     * @return
     */
    Object tryConvert(Object value, Class<?> type);

}
