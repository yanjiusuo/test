package com.jd.workflow.console.service.doc.importer.dto;

import com.jd.workflow.console.entity.MemberRelation;
import lombok.Data;

@Data
public class JApiProjectOwner {
    Long connID;
    String email;
    String fullOrgName;
    String orgName;
    Long ssoID;
    Long userID;
    String userName;
    String userNickName;
    Integer userType;
    public MemberRelation toRelation(){
        if(JapiPartnerUserType.PARTNER_APPLYING.value() == userType
         || JapiPartnerUserType.PARTNER_NONE.value() == userType
        ){
            return null;
        }
        MemberRelation relation = new MemberRelation();
        relation.setUserCode(userName);
        relation.setYn(1);
        final JapiPartnerUserType japiPartnerUserType = JapiPartnerUserType.valueOf(getUserType());
        if(japiPartnerUserType != null){
            relation.setResourceRole(japiPartnerUserType.toResourceRole().getCode());
        }

        return relation;
    }

}
