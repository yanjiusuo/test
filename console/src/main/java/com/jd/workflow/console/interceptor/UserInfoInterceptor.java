package com.jd.workflow.console.interceptor;

import com.jd.common.web.LoginContext;
import com.jd.up.portal.export.api.login.UpLoginContext;
import com.jd.up.portal.login.interceptor.UpLoginContextHelper;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.EmptyUtil;
import com.jd.workflow.console.base.ServiceException;
import com.jd.workflow.console.base.enums.LoginTypeEnum;
import com.jd.workflow.console.dto.UserInfoDTO;
import com.jd.workflow.console.service.IUserInfoService;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 检查是否存在登录态 存在何种登录态，用户是否已经注册登录
 * @date: 2022/6/14 17:12
 * @author wubaizhao1
 */
@Slf4j
@Order(3)
public class UserInfoInterceptor extends BaseInterceptor implements HandlerInterceptor {
    @Resource
    IUserInfoService userInfoService;

    @Value("${interceptor.loginType}")
    private Integer loginTypeCode;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setHeader("Access-Control-Expose-Headers","Location");
        LoginTypeEnum loginType = null;
        //PIN
        /*String pin = JdLoginUtils.getPin(request);
        if(pin!=null){
            loginType = LoginTypeEnum.PIN;
        } else if(LoginTypeEnum.PIN.getCode().equals(loginTypeCode)){
            CommonResult result = CommonResult.buildErrorCodeMsg(1024, "登录校验未通过");
            setHttpJsonBody(result,response);
            return false;
        }*/

        //ERP
        UpLoginContext loginContext = UpLoginContextHelper.getLoginContext();
        log.info("[LoginAop] start，erp:{}", JsonUtils.toJSONString(loginContext));
        if(loginContext!=null){
            loginType = LoginTypeEnum.ERP;
        }

        if(loginType == null){
            throw ServiceException.withCommon("登录校验,登录拦截失效！");
        }
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        switch (loginType){
            /*case PIN:
                Boolean exist = userInfoService.getPin(pin);
                if(!exist){
                    CommonResult commonResult=CommonResult.buildErrorCodeMsg(10240,"登录的账号不在白名单中，请联系管理员处理！");
                    setHttpJsonBody(commonResult,response);
                    return false;
                }
                break;*/
            case ERP:
                LoginContext erpLoginContext = loginContext.getErpLoginContext();

                userInfoDTO.setUserCode(erpLoginContext.getPin());
                userInfoDTO.setUserName(EmptyUtil.isNotEmpty(erpLoginContext.getNick())?erpLoginContext.getNick():"暂无");
                userInfoDTO.setLoginType(loginType.getCode());
                userInfoDTO.setDept(EmptyUtil.isNotEmpty(erpLoginContext.getOrgName())?erpLoginContext.getOrgName():"默认部门");
                userInfoService.checkAndAdd(userInfoDTO);
                break;
        }
        return true;
    }
}
