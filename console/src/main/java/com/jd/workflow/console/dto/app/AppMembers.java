package com.jd.workflow.console.dto.app;

import com.jd.workflow.console.service.remote.api.dto.jdos.JdosAppMembers;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * jdos成员列表
 */
@Data
public class AppMembers {
    /**
     * 负责人
     */
    String owner;
    /**
     * 成员
     */
    Set<String> jdosMembers;
    public static AppMembers from(JdosAppMembers members){
        AppMembers appMembers = new AppMembers();

        Set<String> set = new HashSet<>();
        appMembers.setJdosMembers(set);
        addIfNotEmpty(set,members.getAppAdmin());
        addIfNotEmpty(set,members.getAppOp());
        addIfNotEmpty(set,members.getAppTester());
        addIfNotEmpty(set,members.getSystemAdmin());
        addIfNotEmpty(set,members.getSystemOp());
        addIfNotEmpty(set,members.getSystemOwner());
        addIfNotEmpty(set,members.getSystemTester());
        set.remove(appMembers.getOwner());
        if(members.getAppOwner() != null && members.getAppOwner().size() > 0){
            appMembers.setOwner(members.getAppOwner().get(0));
        }else{
            appMembers.setOwner(null);
        }

        return appMembers;
    }
    private static void addIfNotEmpty(Set<String> set, List<String> list){
        if(list == null ) return;
        set.addAll(list);
    }
}
