//package com.jd.workflow.console.interceptor;
//
///*import com.jd.passport.inteceptor.mvc.SpringMvcInterceptor;
//import com.jd.passport.utils.JDDomainUtils;
//import com.jd.passport.utils.RequestUtils;*/
//import com.jd.up.portal.login.interceptor.HttpRequestUtil;
//import com.jd.up.portal.login.interceptor.UrlBuilder;
//import com.jd.workflow.console.service.IUserInfoService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.annotation.Order;
//
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.net.MalformedURLException;
//
///**
// * 仅校验登录信息是否完整，录入pin进入全局变量
// * 实际拦截以及重定向希望放在 {@link com.jd.workflow.console.aop.LoginAop} 里面
// * @date: 2022/6/9 16:19
// * @author wubaizhao1
// */
//@Slf4j
//@Order(1)
//public class PinInfoInterceptor extends SpringMvcInterceptor {
//
//    @Value("${interceptor.loginType}")
//    private Integer loginType;
//    @Resource
//    IUserInfoService userInfoService;
//
//    /**
//     *
//     */
//    private final String DEFAULT_TENANT = "10001";
//
//    @Override
//    public boolean parseLoginSession(HttpServletRequest request, HttpServletResponse response) {
//
//        return true;
//        /*String path = request.getServletPath();
//        if(!LoginTypeEnum.PIN.getCode().equals(loginType)){
//            log.info("PinInfoInterceptor not pin system pass" );
//            return true;
//        }
//        try {
//            log.info("PinInfoInterceptor->path:{},cookies:{}",path, JsonUtils.toJSONString(request.getCookies()));
//            if (this.needParse || this.needLogin(path)) {
//                FormsAuthenticationTicket ticket = this.parseDotnetTicket(request, response);
//                if (ticket != null && !ticket.isExpired()) {
//                    log.info("PinInfoInterceptor->FormsAuthenticationTicket={}", JsonUtils.toJSONString(ticket));
//                    String pin = ticket.getUsername();
//                    log.info("PinInfoInterceptor->pin={}",pin);
//                    request.setAttribute(JdLoginUtils.PIN, pin);
//                    request.setAttribute("pin", pin);
//
//                    UserInfoInSession userInfoInSession = new UserInfoInSession();
//                    userInfoInSession.setUserId(pin);
//                    userInfoInSession.setUserName(pin);
//                    userInfoInSession.setLoginType(LoginTypeEnum.PIN);
//                    userInfoInSession.setTenantId(DEFAULT_TENANT);
//                    UserSessionLocal.setUser(userInfoInSession);
//
//                    boolean userExist = checkUserExist(pin);
//                    if(!userExist){
//                        CommonResult commonResult=CommonResult.buildErrorCodeMsg(10240,"登录的账号不在白名单中，请联系管理员处理！");
//                        HttpUtils.sendHttpJsonBody(commonResult,response);
//                        return  false;
//                    }
//                }
//                else{
//                    if (this.isAjaxRequest(request)) {
//                        //如果是Ajax的请求，则交由{@link com.jd.workflow.console.aop.LoginAop} 处理
//                        return true;
//                    }
//                    redirect2LoginPage(request, response);
//                    return false;
//                }
//            }
//            return true;
//        } catch (Exception e) {
//            log.error("PinInfoInterceptor--用户拦截器发生异常,request:{}", JsonUtils.toJSONString(request), e);
//            return true;
//        }*/
//    }
//    public boolean checkUserExist(String pin){
//        Boolean exist = userInfoService.getPin(pin);
//        if(!exist){
//
//            return false;
//        }
//        return true;
//    }
//    @Override
//    public void redirect2LoginPage(HttpServletRequest request, HttpServletResponse response) {
//        String encodeCurrentUrl;
//        String logStr;
//        try {
//
//            String currentUrl = RequestUtils.getCurrentUrl(request, this.URIEncoding);
//            encodeCurrentUrl = "";
//            try {
//                encodeCurrentUrl = RequestUtils.encode(currentUrl, this.charsetName);
//                if (RequestUtils.isSSL(request)) {
//                    encodeCurrentUrl = encodeCurrentUrl.replaceAll("http", "https");
//                }
//            } catch (Exception var9) {
//                log.error("RequestUtils.encodeCurrentUrl error!!" + currentUrl + ":" + this.charsetName, var9);
//            }
//
//            logStr = JDDomainUtils.getReturnUrl(this.loginUrl, currentUrl, encodeCurrentUrl);
//
//            response.setHeader("Pragma", "No-cache");
//            response.setHeader("Cache-Control", "no-cache");
//            response.setDateHeader("Expires", 0L);
//            response.sendRedirect(logStr);
//        } catch (Exception var10) {
//            encodeCurrentUrl = RequestUtils.getCurrentURL(request);
//            logStr = RequestUtils.encode(encodeCurrentUrl, "UTF-8");
//            if (RequestUtils.isSSL(request)) {
//                logStr = logStr.replaceAll("http", "https");
//            }
//            String redirectURL = JDDomainUtils.getReturnUrl(this.loginUrl, encodeCurrentUrl, logStr);
//            try {
//                response.sendRedirect(redirectURL);
//            } catch (IOException var8) {
//                log.error("-- redirect to loginPage error for: " + var8.getMessage());
//                throw new RuntimeException(var8);
//            }
//        }
//
//    }
//
//    public void toLoginPage(HttpServletRequest request, HttpServletResponse response, String loginUrl) throws IOException {
//        if (HttpRequestUtil.isAjaxRequest(request)) {
//            response.setStatus(401);
//            response.setHeader("Location", this.getLoginUrl(request, loginUrl));
//        } else {
//            response.sendRedirect(this.getLoginUrl(request, loginUrl));
//        }
//    }
//
//    protected String getLoginUrl(HttpServletRequest request, String _loginUrl) throws MalformedURLException {
//        this.log.info("REQUEST URL:{}", request.getRequestURL());
//        UrlBuilder.Builder currentUrlBuilder = (new UrlBuilder(request.getRequestURL().toString(), "ISO8859-1", true)).forPath((String)null);
//        currentUrlBuilder.put(request.getParameterMap());
//        String loginUrl = StringUtils.isBlank(_loginUrl) ? this.loginUrl : _loginUrl;
//        UrlBuilder.Builder loginUrlBuilder = (new UrlBuilder(loginUrl)).forPath((String)null);
//        loginUrlBuilder.put("ReturnUrl", currentUrlBuilder.build());
//        return loginUrlBuilder.build();
//    }
//}
