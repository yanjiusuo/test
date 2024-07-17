package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.CallHttpToWebServiceReqDTO;
import com.jd.workflow.console.dto.ConvertWebServiceBaseDto;
import com.jd.workflow.console.dto.HttpToWebServiceDTO;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.flow.core.output.HttpOutput;

import java.util.List;

/**
 * 项目名称：example
 * 类 名 称：IServiceConvertService
 * 类 描 述：服务转换
 * 创建时间：2022-05-26 21:03
 * 创 建 人：wangxiaofei8
 */
public interface IServiceConvertService  {

    /**
     * 新增 http转webservice
     * @param httpToWebServiceDTO
     * @return
     */
    public Boolean addHttpToWebService(String basePath,HttpToWebServiceDTO httpToWebServiceDTO);

    /**
     * 修改 http转webservice
     * @param httpToWebServiceDTO
     * @return
     */
    public Boolean modifyHttpToWebService(String basePath,HttpToWebServiceDTO httpToWebServiceDTO);

    /**
     * 删除http转webservice的服务
     * @param id
     * @param interfaceId
     * @return
     */
    public Boolean removeHttpToWebService(Long id,Long interfaceId);

    /**
     * 获取wsdl的内容
     * @param id
     * @param interfaceId
     * @return
     */
    public String getConvertWsdlContent(Long id,Long interfaceId);


    /**
     * 获取详情、编辑时用
     * @param id
     * @param interfaceId
     * @return
     */
    public HttpToWebServiceDTO findHttpToWebService(Long id,Long interfaceId);


    /**
     * 列表功能查询
     * @param methodId
     * @param interfaceId
     * @return
     */
    public List<ConvertWebServiceBaseDto> findHttpToWebServiceList(Long methodId, Long interfaceId);


    /**
     * webservice转http方法调试
     * @param callHttpToWebServiceReqDTO
     * @return
     */
    public Object callHttpToWebService(CallHttpToWebServiceReqDTO callHttpToWebServiceReqDTO);


    public HttpOutput ws2http(Long id,String content);

}
