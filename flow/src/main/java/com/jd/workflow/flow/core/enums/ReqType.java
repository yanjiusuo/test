package com.jd.workflow.flow.core.enums;

import org.apache.http.entity.ContentType;

public enum ReqType {
  xml,form,json,file;
  public String getContentType(){
    if(form.equals(this)){
      return ContentType.APPLICATION_FORM_URLENCODED.getMimeType();
    }else if(json.equals(this)){
      return ContentType.APPLICATION_JSON.getMimeType();
    }else{
      return ContentType.APPLICATION_XML.getMimeType();
    }
  }
}
