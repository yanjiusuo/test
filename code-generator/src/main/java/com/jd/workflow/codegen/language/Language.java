package com.jd.workflow.codegen.language;

import com.jd.workflow.soap.common.util.ObjectHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
@AllArgsConstructor
public enum Language {

    TS("typescript","ts语言"),
    JAVA("java","java语言");


    @Getter
    @Setter
    private String type;


    @Getter
    @Setter
    private String desc;


   public static Language from(String type){
       for (Language value : values()) {
           if(ObjectHelper.equals(type,value.name().toLowerCase())
           || ObjectHelper.equals(type,value.getType())
           ){
               return value;
           }
       }
       return null;
   }

}
