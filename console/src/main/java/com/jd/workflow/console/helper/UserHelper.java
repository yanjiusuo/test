package com.jd.workflow.console.helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jd.official.omdm.is.hr.HrOrganizationService;
import com.jd.official.omdm.is.hr.HrUserService;
import com.jd.official.omdm.is.hr.vo.OrganizationVo;
import com.jd.official.omdm.is.hr.vo.UserVo;
import com.jd.official.omdm.utils.MD5Util;
import com.jd.workflow.console.dto.user.AllUserResponseVO;
import com.jd.workflow.console.dto.user.ResponseBodyVO;
import com.jd.workflow.console.dto.user.ResponseVO;
import com.jd.workflow.console.entity.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 项目名称：parent
 * 类 名 称：UserHelper
 * 类 描 述：TODO
 * 创建时间：2022-11-21 11:09
 * 创 建 人：wangxiaofei8
 */
@Service
@Slf4j
public class UserHelper {

    @Value("${hr.appCode}")
    private String appCode;

    @Value("${hr.businessId}")
    private String businessId;

    @Value("${hr.safetyKey}")
    private String safetyKey;

    @Autowired(required = false)
    private HrUserService hrUserService;
    @Autowired(required = false)
    private HrOrganizationService hrOrganizationService;

    public UserVo getUserBaseInfoByUserName(String erp){
        UserVo userVo = null;
        String responseFormat = "JSON";         //返回类型
        String requestTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.FFF").format(new Date());
        try {
            String sign = MD5Util.getSign(appCode, businessId, requestTimestamp, safetyKey, erp);
            //String result = hrUserService.getUserExtendInfoByUserName(appCode, businessId, requestTimestamp, sign, responseFormat, erp);
            String result = hrUserService.getAllUserTypeByUserName(appCode, businessId, requestTimestamp, sign, responseFormat, erp);
            //log.info("extend user info : {}", result);

            if (StringUtil.isNotBlank(result)) {
                result = URLDecoder.decode(result, "utf-8");
                log.info("decode extent user info : {}", result);
                JSONObject jsonObject = JSON.parseObject(result);
                Object responsebody = jsonObject.get("responsebody");
                if (null != responsebody && StringUtil.isNotBlank(responsebody.toString())) {
                    userVo = JSON.parseObject(responsebody.toString(), UserVo.class);
                }
            }
        } catch (Exception e) {
            log.error("UserHelper.getUserBaseInfoByUserName occur exception >>>>>>",e);
        }
        return userVo;
    }


    /**
     * 根据用户名，从erp系统中进行模糊查询，返回列表
     *
     * @param username
     */
    public List<UserInfo> findUsers(String username) {
        try {
            List<UserInfo> autocompleteList = new ArrayList<UserInfo>();


            String responseFormat = "JSON"; //返回类型

            ObjectMapper objectMapper = new ObjectMapper();
            String requestTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.FFF").format(new Date());
            Map<String, Object> temp = new HashMap<String, Object>();
            temp.put("pageNo", 1);
            temp.put("pageSize", 10);

            String name = username.trim();
            String regex = "^[\\da-zA-Z.]+$"; // 匹配 英文、数字
            if (!name.matches(regex)) {
                temp.put("realName", name);
            } else {
                temp.put("userName", name);
            }

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("params", temp);

            String strVal = objectMapper.writeValueAsString(params);
            String sign = MD5Util.getSign(appCode, businessId, requestTimestamp, safetyKey, strVal);

            String result = hrUserService.findUsers(appCode, businessId, requestTimestamp, sign, responseFormat, strVal);

            log.debug("edLog ==> findUsers -> result：" + JSON.toJSONString(result));
            List<UserVo> userVoList = new ArrayList<UserVo>();
            if (StringUtils.isNotEmpty(result)) {
                ResponseVO responseVO = JSON.parseObject(URLDecoder.decode(result, "utf-8"), ResponseVO.class);
                log.info("response vo : {}", JSON.toJSONString(responseVO));
                if (responseVO.getResStatus().equals("200")) {
                    ResponseBodyVO responseBodyVO = responseVO.getResponsebody();
                    userVoList = responseBodyVO.getUserVoList();
                }
            }

            String virSign = MD5Util.getSign(appCode, businessId, requestTimestamp, safetyKey, username);
            String virUser = hrUserService.getAllUserTypeByUserName(appCode, businessId, requestTimestamp, virSign, responseFormat, username);
            log.info("vir user params app code : {}", appCode);
            log.info("vir user params business id : {}", businessId);
            log.info("vir user params request time : {}", requestTimestamp);
            log.info("vir user params safety key : {}", safetyKey);
            log.info("vir user params sign : {}", virSign);
            log.info("vir user params response format : {}", responseFormat);
            log.info("vir user params username : {}", username);

            log.info("vir user params virUser : {}", URLDecoder.decode(virUser, "utf-8"));

            if (StringUtils.isNotEmpty(virUser)) {
                AllUserResponseVO responseVO = JSON.parseObject(URLDecoder.decode(virUser, "utf-8"), AllUserResponseVO.class);
                log.info("vir response vo : {}", JSON.toJSONString(responseVO));
                if (responseVO.getResStatus().equals("200")) {
                    UserVo userVo = responseVO.getResponsebody();
                    if (userVo != null && userVo.getUserCode() != null && userVo.getUserName() != null) {
                        boolean exists = false;
                        //userVoList.add(userVo);
                        for (UserVo vo : userVoList) {
                            if (vo.getUserName() != null && vo.getUserName().equals(userVo.getUserName())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            userVoList.add(userVo);
                        }
                    }
                }
            }

            int i = 0;
            for (UserVo user : userVoList) {
                i++;
                UserInfo userVO = new UserInfo();
                userVO.setId(Long.valueOf(i));
                userVO.setUserCode(user.getUserName());
                userVO.setDept(user.getOrganizationFullName());
                userVO.setUserName(user.getRealName());
                userVO.setHeadImg(user.getHeadImg());

                autocompleteList.add(userVO);
            }

            return autocompleteList;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<OrganizationVo> getDeptByParentCode(String parentOrganizationCode) {
        List<OrganizationVo> organizationVos = null;
        String responseFormat = "JSON";         //返回类型
        String requestTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.FFF").format(new Date());

        try {
            String sign = MD5Util.getSign(appCode, businessId, requestTimestamp, safetyKey, parentOrganizationCode);
            String result = hrOrganizationService.findChildOrganizations(appCode, businessId, requestTimestamp, sign, responseFormat, parentOrganizationCode);
            log.info("getDeptByParentCode.result={}", result);

            if (org.apache.commons.lang.StringUtils.isNotEmpty(result)) {
                result = URLDecoder.decode(result, "utf-8");
                log.info("getDeptByParentCode.decode#result={}", result);
                JSONObject jsonObject = JSON.parseObject(result);
                Object responsebody = jsonObject.get("responsebody");

                log.info("getDeptByParentCode.responsebody={}", responsebody);

                if (null != responsebody && org.apache.commons.lang.StringUtils.isNotEmpty(responsebody.toString())) {
                    organizationVos = JSON.parseArray(responsebody.toString(), OrganizationVo.class);
                }
            }
        } catch (UnsupportedEncodingException e) {
            log.error("getDeptByParentCode=" + parentOrganizationCode, e);
        }
        return organizationVos;
    }

}
