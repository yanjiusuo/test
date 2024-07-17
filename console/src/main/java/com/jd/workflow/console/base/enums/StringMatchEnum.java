package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
public enum StringMatchEnum {
    NUMBER_LETTER_CN("[0-9a-zA-Z\\u4e00-\\u9fa5]+"),
    NUMBER_LETTER("[0-9_a-zA-Z-]+"),
    LETTER_BEGIN_NUMBER_LETTER("[a-zA-Z][0-9a-zA-Z]+"),
    /**
     * java类名 方法名 等的校验，字母开头
     * @date: 2022/6/20 11:39
     * @author wubaizhao1
     */
    JAVA_PK_NAME("[a-zA-Z][0-9a-zA-Z.]+"),
    JAVA_METHOD_NAME("[a-zA-Z][0-9a-zA-Z]+"),
    ;

    private String match;

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    /**
     * 格式校验，true认为符合格式，false认为不符合格式
     * @param str
     * @return
     */
    public Boolean doMatch(String str){
        if(StringUtils.isBlank(str)){
            return Boolean.FALSE;
        }
        return str.matches(this.getMatch());
    }

    public static void main(String[] args) {
        final Boolean result = StringMatchEnum.NUMBER_LETTER.doMatch("ffs");
        System.out.println(result);
    }
}
