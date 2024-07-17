package com.jd.workflow.console.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.workflow.console.base.*;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.LoginTypeEnum;
import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.dto.client.InterfaceOutDto;
import com.jd.workflow.console.dto.client.MethodOutDto;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.IUserInfoService;
import com.jd.workflow.console.service.client.ClientGwService;
import com.jd.workflow.console.utils.SignHelper;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StringHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wubaizhao1
 * @date: 2022/6/1 14:26
 */
@RestController
@UmpMonitor
@Api(value="对外提供接口",tags="对外提供接口")
public class ClientGwController {
    private static Logger logger = LoggerFactory.getLogger(ClientGwController.class);



    private static final String FUNCTIONID = "functionId";
    private static final String BODY = "body";

    private static final String USER_CODE = "userCode";
    private static final String USER_NAME = "userName";
    @Autowired
    ClientGwService clientGwService;
    @Resource
    IUserInfoService userInfoService;

    @Autowired
    IMethodManageService methodManageService;

    @Value("${sign.validate_range:1000}")
    private Integer validateSignRange;
    /**
     * {@link SignUtil#decrypt}
     * {@link SignUtil#encrypt}
     */
    private static final String SECRET = "E356E752A49608743BC4F7B16D959672";
    private static final String IVSTR = "A4960369B16D3696";

    private static final Map<String, Class> functionMap = new HashMap<>();
    private static final Map<String, Class[]> argsTypesMap = new HashMap<>();

    private static void register(String str, Class clazz, Class[] argsTypes) {
        functionMap.put(str, clazz);
        argsTypesMap.put(str, argsTypes);
    }

    //注册天空之城需要的接口
    @PostConstruct
    private void init() {
        register("test_index", HelloController.class, new Class[]{});
        register("interfaceManage_pageListMyInterface", InterfaceManageController.class, new Class[]{InterfaceManageDTO.class});
        register("methodManage_pageMethod", MethodManageController.class, new Class[]{MethodManageDTO.class});
        register("methodManage_getById", MethodManageController.class, new Class[]{Long.class});
        register("methodManage_invokeMethod", MethodManageController.class, new Class[]{InvokeMethodDTO.class});
        register("methodManage_invokeWebService", MethodManageController.class, new Class[]{CallHttpToWebServiceReqDTO.class, HttpServletRequest.class});
    }


    @RequestMapping("client")
    public Object client(HttpServletRequest request, HttpServletResponse response) {
        logger.info("client->start");
        String functionId = request.getParameter(FUNCTIONID);

        String body = request.getParameter(BODY);
        String userCode = request.getParameter(USER_CODE);
        String userName = request.getParameter(USER_NAME);

        try {
            UserInfoInSession userInfoInSession = new UserInfoInSession();
            UserInfoDTO userInfoDTO = new UserInfoDTO();
            //全局用户信息
            userInfoInSession.setUserId(userCode);
            userInfoInSession.setUserName(userName);
            userInfoInSession.setLoginType(LoginTypeEnum.HEALTH);
            userInfoInSession.setTenantId("1003");
            UserSessionLocal.setUser(userInfoInSession);
            //新增用户
            userInfoDTO.setUserCode(userCode);
            userInfoDTO.setUserName(userName);
            userInfoDTO.setLoginType(LoginTypeEnum.HEALTH.getCode());
            userInfoDTO.setDept("京东健康");
            userInfoService.checkAndAdd(userInfoDTO);

            //对body解密
            try {
                body = SignUtil.decrypt(body, SECRET, IVSTR);
            } catch (Exception e) {
                logger.error("client error,解密失败,functionId=[{}],param:{}", functionId, e);
                return CommonResult.error("解密错误!");
            }

            // 参数
            Object[] args = getBody(body, functionId, request);
            logger.info("client begin2,functionId=[{}],body:{},cookies:{}", functionId, JsonUtils.toJSONString(args), JsonUtils.toJSONString(request.getCookies()));

            // bean 类对象
            Class clzBean = functionMap.get(functionId);
            Object object = SpringContextUtil.getApplicationContext().getBean(clzBean);

            String methodName = functionId.substring(functionId.lastIndexOf("_") + 1);
            logger.info("client->methodName:{}", methodName);
            List<Class> classList = new ArrayList<>();
            for (Object arg : args) {
                classList.add(arg.getClass());
            }
            // 参数类型
            Class[] clzTypes = argsTypesMap.get(functionId);
            Method method = ReflectionUtils.findMethod(object.getClass(), methodName, clzTypes);
            logger.info("client.action functionId=[{}], args:{}", functionId, JsonUtils.toJSONString(args));
            // 调用方法
            Object result = ReflectionUtils.invokeMethod(method, object, args);
            logger.info("client end,functionId=[{}],result:{}", functionId, JsonUtils.toJSONString(result));
            return result;
        } catch (Exception e) {
            logger.error("client error,functionId=[{}],param:{}", functionId, e);
        } finally {

        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE,PUT");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
        return CommonResult.error("未知异常!");
    }

    /**
     * body信息封装pin信息
     *
     * @param request
     * @return
     */
    private Object[] getBody(String body, String functionId, HttpServletRequest request) {

        logger.info("client begin1,body:{}", body);
        if (StringUtils.isBlank(body)) {
            return new Object[0];
        }
        List<Object> objects = new ArrayList<>();
        List<String> stringList = JsonUtils.parseArray(body, String.class);
        int i = 0;
        Class[] clzTypes = argsTypesMap.get(functionId);
        for (Class clzType : clzTypes) {
            if (clzType.equals(HttpServletRequest.class)) {
                objects.add(request);
            } else {
                Object parse = JsonUtils.parse(stringList.get(i++), clzType);
                objects.add(parse);
            }
        }
        return objects.toArray();
    }
//    private Boolean checkSignEquals(String sign,String... params){
//        String md5 = SignUtil.createMD5(params);
//        if(sign.equals(md5)){
//            return Boolean.TRUE;
//        }else{
//            return Boolean.FALSE;
//        }
//    }

    /**
     *
     * @param request
     * @param lastModified 上次修改时间
     * @param current 分页当前页数
     * @param size 分页pageSize
     * @param sign 接口签名 把当前参数加上t(当前时间戳)按照字典序排序完成后，编码成queryString,然后用aes算法加密
     * @return 接口列表
     */
    @ApiOperation("查询全部接口列表")
    @RequestMapping("/doc/external/queryInterfaces")
    public CommonResult<IPage<InterfaceOutDto>> queryInterfaces(HttpServletRequest request,
                                                                Long lastModified, Long current, Long size,String sign) {
        Guard.notEmpty(current,"current不可为空");
        Guard.notEmpty(size,"size不可为空");
        Guard.notEmpty(sign,"sign不可为空");
        Guard.assertTrue(size <=1000,"最大条数不能超过1000");
        validateSign(request);
        Page<InterfaceOutDto> result = clientGwService.queryInterfaceIds(lastModified, current, size, InterfaceTypeEnum.HTTP.getCode());

        return CommonResult.buildSuccessResult(result);
    }

    /**
     * 根据接口id查询接口基本信息
     * @param request
     * @param interfaceId 要操作的接口id
     * @param sign 接口签名 把当前参数加上t(当前时间戳)按照字典序排序完成后，编码成queryString,然后用aes算法加密
     * @return
     */
    @ApiOperation("根据接口id查询接口基本信息")
    @RequestMapping("/doc/external/queryInterfaceById")
    public CommonResult<InterfaceOutDto> queryInterfaceById(HttpServletRequest request,
                                                                Long interfaceId,String sign) {
        Guard.notEmpty(interfaceId,"interfaceId不可为空");
        Guard.notEmpty(sign,"sign不可为空");
        validateSign(request);
        InterfaceOutDto result = clientGwService.queryInterfaceById(interfaceId);

        return CommonResult.buildSuccessResult(result);
    }
    /**
     *
     * @param request
     * @param lastModified 上次修改时间
     * @param current 分页当前页数
     * @param size 分页pageSize
     * @param sign 接口签名 把当前参数加上t(当前时间戳)按照字典序排序完成后，编码成queryString,然后用aes算法加密
     * @return
     */
    @ApiOperation("查询全部方法列表")
    @RequestMapping("/doc/external/queryMethods")
    public CommonResult<IPage<MethodOutDto>> queryMethods(HttpServletRequest request,
                                                                Long lastModified, Long current, Long size,String sign) {
        Guard.notEmpty(current,"current不可为空");
        Guard.notEmpty(size,"size不可为空");
        Guard.assertTrue(size <=1000,"最大条数不能超过1000");
        Guard.notEmpty(sign,"sign不可为空");
        validateSign(request);
        Page<MethodOutDto> result = clientGwService.getMethods(lastModified, current, size, InterfaceTypeEnum.HTTP.getCode());

        return CommonResult.buildSuccessResult(result);
    }

    void validateSign(HttpServletRequest request) {
        String sign = request.getParameter("sign");

        Map<String, Object> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String[] values = entry.getValue();
            if (values == null || values.length == 0) {
                params.put(entry.getKey(), null);
            } else {
                params.put(entry.getKey(), values[0]);
            }
        }
        boolean valid = SignHelper.validateSign(sign, validateSignRange);
        try {
            String validSign = SignHelper.signParams(params);
            logger.info("external.valid_sign={},request={},current={}", validSign, params, sign);
        } catch (Exception e) {
        }
        if (StringHelper.isBlank(sign)) {
            throw new BizException("sign不可为空!");
        }
        Guard.assertTrue(valid, "验签无效", -1);

    }

    public static void main(String[] args) {

    }
}
