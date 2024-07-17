package com.jd.workflow.console.interceptor;

import com.jd.up.portal.export.api.login.UpLoginCheckParam;
import com.jd.up.portal.export.api.login.UpLoginContext;
import com.jd.up.portal.export.api.login.UpLoginExportApiService;
import com.jd.up.portal.login.interceptor.UpCommonLoginInterceptor;
import com.jd.up.portal.login.interceptor.UpLoginContextHelper;
import com.jd.workflow.console.base.EmptyUtil;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.LoginTypeEnum;
import com.jd.workflow.console.dto.UserInfoDTO;
import com.jd.workflow.console.service.IUserInfoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.web.cors.CorsUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * copy by {@link UpLoginCheckParam}
 * 存在自定义的需求
 * 实际拦截以及重定向希望放在 里面
 * @date: 2022/6/9 18:43
 * @author wubaizhao1
 */
@Slf4j
@AllArgsConstructor
@Order(2)
public class ErpInfoInterceptor extends UpCommonLoginInterceptor {

    private UpLoginExportApiService upLoginApiService;

    @Value("${tenant.allowTenantIds:}")
    private String allowTenantIds;

    @Value("${tenant.fixTenantId:}")
    private String fixTenantId;

    private List<String> allowTenantIdList;

    @Resource
    IUserInfoService userInfoService;

    @Value("${interceptor.loginType}")
    private Integer loginType;

    public Integer getLoginType() {
        return loginType;
    }

    public ErpInfoInterceptor() {
    }
    @PostConstruct
    public void init(){
        allowTenantIdList = new ArrayList<>();
        if(StringUtils.isNotBlank(allowTenantIds)){
            allowTenantIdList = Arrays.asList(StringUtils.split(allowTenantIds,','));
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(!LoginTypeEnum.ERP.getCode().equals(loginType)){
            log.info("ErpInfoInterceptor not Erp system pass" );
            return true;
        }
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        } else {
            boolean res = super.preHandle(request, response, handler);
            UpLoginContext context = UpLoginContextHelper.getLoginContext();
            if(Objects.nonNull(context)){
                UserInfoInSession userInfoInSession=new UserInfoInSession();
                userInfoInSession.setUserId(context.getPin());
                userInfoInSession.setUserName(EmptyUtil.isNotEmpty(context.getNick())?context.getNick():"暂无");
                userInfoInSession.setDept(EmptyUtil.isNotEmpty(context.getOrgName())?context.getOrgName():"暂无");
                userInfoInSession.setLoginType(LoginTypeEnum.ERP);
                String tenantCode = getTenantCode(request, context);
                userInfoInSession.setTenantId(tenantCode);
                userInfoInSession.setTenantKey(context.getTenantId());
                UserSessionLocal.setUser(userInfoInSession);
                checkAndAddUser(userInfoInSession);
            }
            return res;
        }
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
        UserSessionLocal.removeUser();
    }

    /**
     *
     * @param request
     * @param apiResponse
     * @return
     */
    private String getTenantCode(HttpServletRequest request, UpLoginContext apiResponse) {
        String tenantId = request.getHeader("tenantId");
        if(StringUtils.isNotBlank(tenantId)){
            log.info("erp.use_header_tenantId:tenantId={}", tenantId);
            return tenantId;
        }else  if(StringUtils.isNotBlank(fixTenantId)){
            return fixTenantId;
        }else{
            if(StringUtils.isNotBlank(tenantId)
                    && allowTenantIdList.contains(tenantId)
            ){
                return tenantId;
            }else{
                tenantId =  EmptyUtil.isNotEmpty(apiResponse.getTenantCode())? apiResponse.getTenantCode() : "-1" ;
                return tenantId;
            }
        }
    }

    /**
     * 待优化
     * @param session
     */
    public void checkAndAddUser(UserInfoInSession session){
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setUserCode(session.getUserId());
        userInfoDTO.setUserName(session.getUserName());
        userInfoDTO.setLoginType(loginType);
        userInfoDTO.setDept(session.getDept());
        userInfoService.checkAndAdd(userInfoDTO);
    }

    public void setUpLoginApiService(UpLoginExportApiService upLoginExportApiService) {
        this.upLoginApiService = upLoginExportApiService;
    }
}

