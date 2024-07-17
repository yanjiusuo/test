package com.jd.workflow.console.utils;

import com.jd.workflow.console.dto.Version;

public class VersionManager {
    public static String increaseVersion(String versionStr,int pos){
         Version version = Version.parse(versionStr);
         version.increase(pos);
         return version.toString();
    }
}
