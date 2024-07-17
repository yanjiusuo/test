package com.jd.workflow.console.service.role;

import com.jd.common.util.StringUtils;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dto.role.UserRoleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/5/29
 */
@Service
public class AdminHelper {

    @Autowired
    private AccRoleServiceAdapter accRoleServiceAdapter;

    /**
     * 是管理员
     * @return
     */
    public Boolean isNormalAdmin() {
        Boolean retBol = Boolean.FALSE;
        String erp = UserSessionLocal.getUser().getUserId();
        if (StringUtils.isNotBlank(erp)) {
            UserRoleDTO userRoleDTO = accRoleServiceAdapter.queryUserRole(erp);
            if (userRoleDTO.getJapiAdmin() || userRoleDTO.getConsoleAdmin() || userRoleDTO.isTenantManager()) {
                retBol = Boolean.TRUE;
            }
        }
        return retBol;

    }

}
