package com.jd.workflow.soap.common.util.json;


public class RawString {
    String str;
    public RawString(){

    }

    public RawString(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }
}

