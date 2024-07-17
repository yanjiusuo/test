package com.jd.workflow.console.helper;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.soap.common.util.StringHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 项目名称：example
 * 类 名 称：ProjectHelper
 * 类 描 述：系统工具类
 * 创建时间：2022-06-07 15:03
 * 创 建 人：wangxiaofei8
 */
@Service
public class ProjectHelper {


    @Resource
    private IInterfaceManageService interfaceManageService;

    @Resource
    private IMethodManageService methodManageService;
    /**
     * 推送ducc提前初始化的cjg应用id
     */
    @Value("${camel.config.appId:integration-paas}")
    private String camelConfigAppId;

    /**
     * 发布地址前缀域名
     */
    @Value("${publish.address.prefix}")
    private String publishAddressPrefix;

    /**
     * 发布地址匹配表达式
     */
    private String PUBLISH_ADDRESS_FORMAT = "%s/api/routeService/%s";


    /**
     * 转换后webservice的wsdl路径
     * wsdL mock url path
     */
    private String MOCK_WSDL_PATH_FORMAT = "/serviceConvert/getConvertWsdlContent?id=%s&interfaceId=%s";


    /**
     * 获得wsdl路径
     *
     * @param id
     * @param interfaceId
     * @return
     */
    public String getMockWsdlPath(Long id, Long interfaceId) {
        return String.format(MOCK_WSDL_PATH_FORMAT, id, interfaceId);
    }

    /**
     * 获得发布后的调用地址
     *
     * @param publishMethodId
     * @return
     */
    public String getPublishUrl(String publishMethodId) {
        publishMethodId =StringHelper.replace(publishMethodId,"$","/");


        if(publishMethodId.startsWith("/")){
            publishMethodId = publishMethodId.substring(1);
        }
        return String.format(PUBLISH_ADDRESS_FORMAT, publishAddressPrefix, publishMethodId);
    }

    /**
     * 获取发布地址
     * @param domain
     * @param publishMethodId
     * @return
     */
    public String getPublishUrl(String domain,String publishMethodId) {
        if(publishMethodId.startsWith("/")){
            publishMethodId = publishMethodId.substring(1);
        }
        publishMethodId = StringHelper.replace(publishMethodId,"$","/");
        return String.format(PUBLISH_ADDRESS_FORMAT, domain, publishMethodId);
    }


    /**
     * 获取方法的发布路径
     *
     * @param methodId
     * @param interfaceId
     * @return
     */
    public String getPublishMethodId(Long methodId, Long interfaceId) {
        MethodManage methodManage = methodManageService.getById(methodId);
        InterfaceManage interfaceManage = interfaceManageService.getOneById(interfaceId);

        return getPublishMethodId(methodManage,interfaceManage);
    }
    public String getPublishMethodId(MethodManage methodManage, Long interfaceId) {

        InterfaceManage interfaceManage = interfaceManageService.getOneById(interfaceId);
        return getPublishMethodId(methodManage,interfaceManage);
    }
    public String getPublishMethodId(MethodManage methodManage, InterfaceManage interfaceManage) {

        if (!StringUtils.isBlank(interfaceManage.getServiceCode())
                && !StringUtils.isBlank(methodManage.getMethodCode())
        ) {
            return "$" + interfaceManage.getServiceCode() + "$" + methodManage.getMethodCode();
        }
        return methodManage.getId() + "";
    }


}
