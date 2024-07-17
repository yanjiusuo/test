package com.jd.workflow.console.dto.requirement;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/31
 */

import com.jd.workflow.console.base.enums.ResourceRoleEnum;
import com.jd.workflow.console.entity.MemberRelation;
import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/31 
 */
@Data
public class UserInfoDTO {

    /**
     * erp
     */
    private String erp;
    /**
     * 中文名
     */
    private String userName;
    /**
     * 部门名称
     */
    private String deptName;
    private Boolean isOwner;

    /**
     * 加入时间
     */
    private Date modified;
    public static UserInfoDTO from(MemberRelation relation){
        UserInfoDTO dto = new UserInfoDTO();
        dto.setErp(relation.getUserCode());
        dto.setUserName(relation.getUserName());
        dto.setDeptName(relation.getDeptName());
        dto.setModified(relation.getModified());
        dto.setIsOwner(ResourceRoleEnum.ADMIN.getCode().equals(relation.getResourceRole()));
        return dto;
    }


    @Override
    public boolean equals(final Object obj){
        if (obj == null) {
            return false;
        }
        final UserInfoDTO userInfoDTO = (UserInfoDTO) obj;
        if (this == userInfoDTO) {
            return true;
        } else {
            return (this.erp.equals(userInfoDTO.getErp()));
        }

    }
    @Override
    public int hashCode() {
        int hashno = 7;
        hashno = 13 * hashno + (erp == null ? 0 : erp.hashCode());
        return hashno;
    }
}
