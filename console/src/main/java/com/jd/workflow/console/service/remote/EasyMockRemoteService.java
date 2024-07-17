package com.jd.workflow.console.service.remote;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.MeasureDataEnum;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.dto.flow.param.JsfOutputExt;
import com.jd.workflow.console.dto.jsf.JsfDebugData;
import com.jd.workflow.console.dto.jsf.NewJsfDebugDto;
import com.jd.workflow.console.dto.mock.HttpDemoValue;
import com.jd.workflow.console.dto.mock.JsfDemoValue;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.debug.HttpDebugDataDto;
import com.jd.workflow.console.service.measure.IMeasureDataService;
import com.jd.workflow.console.utils.ReqDemoBuildUtils;
import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.utils.ParamMappingContext;
import com.jd.workflow.flow.utils.ParametersUtils;
import com.jd.workflow.jsf.analyzer.JarParser;
import com.jd.workflow.jsf.analyzer.MavenJarLocation;
import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.jsf.cast.JsfParamConverterRegistry;
import com.jd.workflow.jsf.cast.impl.PrimitiveParamConverter;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.xml.XNode;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import com.jd.workflow.soap.common.xml.schema.ValueBuilderAcceptor;
import com.jd.y.jsf.HttpMckJsf;
import com.jd.y.jsf.JsfMockOpenAPI;
import com.jd.y.model.dto.*;
import com.jd.y.model.qo.JsfMethodOpenQO;
import com.jd.y.model.qo.JsfTemplateOpenQO;
import com.jd.y.model.vo.*;
import com.jd.y.response.ReplyVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class EasyMockRemoteService {
    static final String METHOD_PREFIX = "/PAAS";
    public static final String NEW_METHOD_PREFIX = "/PAAS_APP";
    static final String DEFAULT_MOCK_TEMPLATE = "默认mock模板";
    static final String DEFAULT_SERVICE_CODE = "_default";
    static final int PLATFORM_PAAS = 2;
    public static final String DEFAULT_ALIAS = "easymock_PaaS_test";
    static Integer HTTP_TEMPLATE_DEFAULT_ENABLED = 1;
    ParametersUtils utils = new ParametersUtils();

    HttpMckJsf httpMockJsf;

    JsfMockOpenAPI jsfMockOpenAPI;
    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    MockDataBuildService mockDataBuildService;

    @Autowired
    IMethodManageService methodManageService;
    @Autowired
    IAppInfoService appInfoService;

    @Autowired
    private IMeasureDataService measureDataService;

    public void setHttpMockJsf(HttpMckJsf httpMockJsf) {
        this.httpMockJsf = httpMockJsf;
    }

    public void setJsfMockOpenAPI(JsfMockOpenAPI jsfMockOpenAPI) {
        this.jsfMockOpenAPI = jsfMockOpenAPI;
    }

    public void setInterfaceManageService(IInterfaceManageService interfaceManageService) {
        this.interfaceManageService = interfaceManageService;
    }

    public void setMethodManageService(IMethodManageService methodManageService) {
        this.methodManageService = methodManageService;
    }


    public void methodSwitch(MethodManage method, boolean delivery) {
        InterfaceManage interfaceManage = interfaceManageService.getById(method.getInterfaceId());
        Integer relatedInterfaceId = queryJsfInterfaceById(interfaceManage);
        Integer relatedJsfMethodId = queryJsfMethodId(relatedInterfaceId, method.getMethodCode());
        JsfMethodOpenDto dto = new JsfMethodOpenDto();
        dto.setMethodId(relatedJsfMethodId);
        dto.setOperator(UserSessionLocal.getUser().getUserId());
        JsfUpdateModel jsfUpdateModel = new JsfUpdateModel();
        dto.setJsfUpdateModel(jsfUpdateModel);
        if (delivery) {  // 开启透传,methodSwitch对应0
            jsfUpdateModel.setMethodSwitch(0); // 0表示不启用mock方法，也就是透传
        } else {
            jsfUpdateModel.setMethodSwitch(1);
        }
        final ReplyVO<Void> voidReplyVO = jsfMockOpenAPI.methodSwitch(dto);
        if (!isSuccess(voidReplyVO)) {
            throw new BizException("切换失败:" + voidReplyVO.getMessage());
        }
    }

    /**
     * 查询jsf接口
     *
     * @param manage
     * @return
     */
    public Integer queryJsfInterfaceById(InterfaceManage manage) {

        JsfInterfaceOpenVO vo = queryJsfInterface(manage);
        if (vo != null) {
            return vo.getId();
        }
        return null;
    }

    public JsfInterfaceOpenVO queryJsfInterface(InterfaceManage manage) {
        JsfInterfaceOpenDTO dto = new JsfInterfaceOpenDTO();
        dto.setInterfaceName(manage.getServiceCode());
        dto.setMavenInfo(getMavenInfo(manage.getPath()));
        dto.setMockedAlias(EasyMockRemoteService.DEFAULT_ALIAS);

        final ReplyVO<List<JsfInterfaceOpenVO>> replyVo = jsfMockOpenAPI.queryInterface(dto);
        if (isSuccess(replyVo) && !CollectionUtils.isEmpty(replyVo.getData())) {
            if (replyVo.getData().get(0) == null) return null;
            return replyVo.getData().get(0);
        }
        return null;
    }

    private String parseMavenInfo(String mavenInfo) {
        try {
            XNode node = XNode.parse(mavenInfo);
            XNode groupIdNode = node.childByTag("groupId");
            XNode artifactIdNode = node.childByTag("artifactId");
            XNode versionNode = node.childByTag("version");
            if (groupIdNode != null && artifactIdNode != null && versionNode != null) {
                return groupIdNode.value() + ":" + artifactIdNode.value() + ":" + versionNode.value();
            }
        } catch (Exception e) {
            log.error("xml.err_parse_maven_info:mavenInfo={}", mavenInfo, e);
        }


        return null;
    }

    private boolean validateValidMavenPath(String path) {
        MavenJarLocation location = new MavenJarLocation();
        String[] paths = StringUtils.split(path, ':');
        location.setGroupId(paths[0]);
        location.setArtifactId(paths[1]);
        location.setVersion(paths[2]);

        boolean isValidLocation = JarParser.isValidMavenLocation(location);
        if (!isValidLocation) {
            throw new BizException("无效的maven坐标：" + path + "，请确保jar包已上传");
        }
        return isValidLocation;
    }

    /**
     * 添加jsf接口
     *
     * @param manage
     * @return jsf方法id
     */
    private JsfInterfaceOpenVO addJsfInterface(InterfaceManage manage) {

        validateValidMavenPath(manage.getPath());

        final UserInfoInSession user = UserSessionLocal.getUser();
        JsfInterfaceOpenDTO jsfDto = new JsfInterfaceOpenDTO();
        jsfDto.setCreatedBy(user.getUserId());
        jsfDto.setInterfaceName(manage.getServiceCode());
        jsfDto.setJarDownloadType(1);
        jsfDto.setMockedAlias(DEFAULT_ALIAS);
        jsfDto.setPlatform(PLATFORM_PAAS + "");
        jsfDto.setDeliverFlag(1); // 1表示透传,0表示不透传

        jsfDto.setMavenInfo(getMavenInfo(manage.getPath()));
        final ReplyVO<Integer> replyVO = jsfMockOpenAPI.addInterface(jsfDto);
        log.info("jsf.add_interface:interfaceId={},response={}", manage.getId(), JsonUtils.toJSONString(replyVO));
        if (!"0".equals(replyVO.getCode())) {
            log.error("jsf.err_add_interface:interfaceId={},response={}", manage.getId(), JsonUtils.toJSONString(replyVO));
            throw new BizException("mock数据添加失败：" + replyVO.getMessage());
        }
        //manage.setRelatedId(replyVO.getData().longValue());

        JsfInterfaceOpenVO vo = new JsfInterfaceOpenVO();
        vo.setId(replyVO.getData());
        vo.setMockSwitch(0);
        // interfaceManageService.updateById(manage);
        return vo;
    }

    public String openOrCloseInterface(InterfaceManage manage, boolean result) {
        final ReplyVO<String> replyVo = jsfMockOpenAPI.openOrCloseInterface(manage.getServiceCode(), DEFAULT_ALIAS, result);
        log.info("jsf.open_interface_info:name={},open={},result={}", manage.getServiceCode(), result, JsonUtils.toJSONString(replyVo));
        if (!isSuccess(replyVo)) {
            return replyVo.getMessage();
        }
        return null;
    }

    public JsfInterfaceOpenVO addOrUpdateJsfInterface(InterfaceManage manage) {
        return addOrUpdateJsfInterface(manage, true);
    }

    /**
     * 更新jsf接口
     *
     * @param manage
     */
    public JsfInterfaceOpenVO addOrUpdateJsfInterface(InterfaceManage manage, boolean forceUpdate) {
        if (StringUtils.isEmpty(manage.getPath())) {
            throw new BizException("maven坐标为空，无法开启mock，请检查jsf配置");
        }
        Integer id = null;
        JsfInterfaceOpenVO vo = queryJsfInterface(manage);
        if (vo != null) {
            id = vo.getId();
        }
        if (id != null) {
            //manage.setRelatedId(id.longValue());
            String existPath = parseMavenInfo(vo.getMavenInfo());
            if (ObjectHelper.equals(existPath, manage.getPath())) return vo;
            validateValidMavenPath(manage.getPath());
            //if(!forceUpdate) return vo;
            JsfInterfaceOpenDTO jsfDto = new JsfInterfaceOpenDTO();
            jsfDto.setId(id + "");
            jsfDto.setInterfaceName(manage.getServiceCode());
            jsfDto.setMockedAlias(DEFAULT_ALIAS);

            jsfDto.setMavenInfo(getMavenInfo(manage.getPath()));
            final ReplyVO<Void> replyVO = jsfMockOpenAPI.changeInterface(jsfDto);
            if (!"0".equals(replyVO.getCode())) {
                log.error("jsf.err_change_interface:interfaceId={},interfaceName={},response={}", manage.getId(), manage.getServiceCode(), JsonUtils.toJSONString(replyVO));
                throw new BizException("jsf mock数据修改失败：" + replyVO.getMessage());
            }
            log.info("jsf.change_interface:interfaceId={},interfaceName={},response={}", manage.getId(), manage.getServiceCode(), JsonUtils.toJSONString(replyVO));
            return vo;
        } else {
            vo = addJsfInterface(manage);
            return vo;
        }

    }

    public void updateDeliverInfo(InterfaceManage manage, String deliverToken, String deliverAlias) {
        JsfInterfaceOpenDTO jsfDto = new JsfInterfaceOpenDTO();
        if (StringUtils.isEmpty(deliverAlias)) { // 根据透传别名控制开启透传
            jsfDto.setDeliverFlag(0);
        } else {
            jsfDto.setDeliverFlag(1);
        }
        Integer relatedInterfaceId = queryJsfInterfaceById(manage);
        jsfDto.setId(relatedInterfaceId + "");
        if (StringUtils.isBlank(deliverToken)) {
            deliverToken = null;
        }
        jsfDto.setDeliverToken(deliverToken);
        jsfDto.setMavenInfo(getMavenInfo(manage.getPath()));
        jsfDto.setDeliverAlias(deliverAlias);
        final ReplyVO<Void> replyVO = jsfMockOpenAPI.changeInterface(jsfDto);
        if (!isSuccess(replyVO)) {
            log.error("jsf.err_update_delivery_info:relatetdId={},replyVO={}", relatedInterfaceId, JsonUtils.toJSONString(replyVO));
            throw new BizException("更新透传信息失败：" + replyVO.getMessage());
        }
    }

    private boolean isSuccess(ReplyVO replyVO) {
        return "0".equals(replyVO.getCode());
    }

    public void updateHttpMethod(InterfaceManage interfaceManage, MethodManage methodManage) {
        Integer relatedHttpMethodId = queryHttpInterface(interfaceManage, methodManage);
        if (relatedHttpMethodId == null) { // 历史数据
            addHttpMethod(interfaceManage, methodManage);
            return;
        }
        HttpInterfaceDto updateDto = new HttpInterfaceDto();
        updateDto.setId(relatedHttpMethodId);
        updateDto.setName(methodManage.getName());
        updateDto.setUrl(buildUrlPath(interfaceManage, methodManage));
        updateDto.setMethod(getHttpMethod(methodManage.getHttpMethod().toUpperCase()));
        final ReplyVO<HttpInterfaceVO> replyVo = httpMockJsf.updateHttpInterfaceById(updateDto);
        if (!isSuccess(replyVo)) {
            log.error("mock.err_update_http_method:methodId={},dto={},replyVO={}", methodManage.getId(), JsonUtils.toJSONString(updateDto), JsonUtils.toJSONString(replyVo));
            throw new BizException("mock方法信息更新失败：" + replyVo.getMessage());
        }
        log.info("mock.success_update_http_method:methodId={},dto={},replyVO={}", methodManage.getId(), JsonUtils.toJSONString(updateDto), JsonUtils.toJSONString(replyVo));
    }

    public static String getHttpMethod(String method) {
        method = method.toUpperCase();
        if (method.indexOf("GET") != -1) return "GET";
        else if (method.indexOf("POST") != -1) return "POST";
        String[] methods = StringUtils.split(method, ',');
        return methods == null ? "GET" : methods[0];

    }

    public boolean existTemplate(InterfaceManage interfaceManage, MethodManage methodManage) {
        Integer relatedId = null;
        if (InterfaceTypeEnum.HTTP.getCode().equals(methodManage.getType()) || InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(methodManage.getType())) {
            relatedId = queryHttpInterface(interfaceManage, methodManage);
            if (relatedId != null) {
                HttpDataDto dto = new HttpDataDto();
                dto.setHttpId(Integer.valueOf(relatedId));

                ReplyVO<List<HttpDataVO>> replyVO = httpMockJsf.queryHttpTemplateByParams(dto);
                if ("0".equals(replyVO.getCode())) {
                    if (replyVO.getData().size() > 0) {
                        return true;
                    }
                }
            }

        } else if (InterfaceTypeEnum.JSF.getCode().equals(methodManage.getType())) {
            JsfInterfaceOpenVO jsfInterfaceOpenVO = queryJsfInterface(interfaceManage);
            if (jsfInterfaceOpenVO != null) {
                relatedId = queryJsfMethodId(jsfInterfaceOpenVO.getId(), methodManage.getMethodCode());
                if (relatedId != null) {
                    return !ObjectHelper.isEmpty(getJsfTemplates(relatedId));
                }

            }
        }
        return false;
    }

    /**
     * 添加http方法，返回添加记录的id
     *
     * @param interfaceManage
     * @param method
     * @return
     */
    public Integer addHttpMethod(InterfaceManage interfaceManage, MethodManage method) {
        HttpInterfaceVO interfaceVO = queryHttpInterfaceInfo(interfaceManage, method);
        Integer id = null;
        if (interfaceVO != null && interfaceVO.getId() != null) {
            id = interfaceVO.getId();
            log.info("easymock.already_exist_http_path:methodId={},id={}", method.getId(), id);
            //method.setRelatedId(id.longValue());
            //methodManageService.updateById(method);
            if (interfaceVO.getMockSwitch() == null
                    || interfaceVO.getMockSwitch().equals(0)
            ) {
                log.info("easymock.update_non_activef_interface:id={},httpPath={}", method.getId(), method.getPath());
                HttpInterfaceDto updateDto = new HttpInterfaceDto();
                updateDto.setId(id);
                updateDto.setMockSwitch(1);
                ReplyVO<HttpInterfaceVO> updateResult = httpMockJsf.updateHttpInterfaceById(updateDto);
                if (!isSuccess(updateResult)) {
                    throw new BizException("切换http接口到线上状态失败：" + updateResult.getMessage());
                }
            }
            return id;
        }
        final UserInfoInSession user = UserSessionLocal.getUser();
        HttpInterfaceAddDTO httpDto = new HttpInterfaceAddDTO();
        httpDto.setCreatedBy(user.getUserId());
        httpDto.setCreatedTme(new Date());
        httpDto.setPlatform(2);
        httpDto.setMethod(getHttpMethod(method.getHttpMethod().toUpperCase()));
        httpDto.setName(method.getName());
        httpDto.setMockSwitch(1); // mock切换
        httpDto.setUrl(buildUrlPath(interfaceManage, method));

        final ReplyVO<HttpInterfaceVO> replyVo = httpMockJsf.addHttpInterface(httpDto);
        if (!isSuccess(replyVo)) { // 成功了
            log.error("easymock.err_add_http_interface:methodId={},replyVO={}", method.getId(), JsonUtils.toJSONString(replyVo));
            throw new BizException("mock数据增加失败：" + replyVo.getMessage());

        } else {
            log.info("easymock.add_http_interface:methodId={},replyVO={}", method.getId(), JsonUtils.toJSONString(replyVo));
            id = queryHttpInterface(interfaceManage, method);
            //method.setRelatedId(id.longValue());
            //methodManageService.updateById(method);

        }
        return id;
    }

    public static String buildAppUrlPrefix(AppInfo appInfo) {
        if (appInfo == null) {
            ///throw new BizException("未关联应用，无法生成mock前缀");
            return null;
        }
        return NEW_METHOD_PREFIX + "/" + appInfo.getAppCode();
    }

    public String buildUrlPath(InterfaceManage interfaceManage, MethodManage method) {
        Long appId = interfaceManage.getAppId();
        AppInfo appInfo = null;
        if (appId != null) {
            appInfo = appInfoService.getById(appId);

        }
        return buildAppUrlPrefix(appInfo) + method.getPath();


    }

    public HttpInterfaceVO queryHttpInterfaceInfo(InterfaceManage manage, MethodManage method) {
        String url = buildUrlPath(manage, method);
        final ReplyVO<HttpInterfaceVO> replyVo = httpMockJsf.getInterfaceByUrlAndMethod(url, getHttpMethod(method.getHttpMethod().toUpperCase()));
        if (isSuccess(replyVo) && replyVo.getData() != null) {
            return replyVo.getData();
        }
        return null;
    }

    public Integer queryHttpInterface(InterfaceManage manage, MethodManage method) {
        HttpInterfaceVO httpInterfaceVO = queryHttpInterfaceInfo(manage, method);
        if (httpInterfaceVO == null) return null;
        return httpInterfaceVO.getId();
    }

    public JsfMethodOpenVO queryJsfMethod(Integer interfaceId, String methodName) {
        JsfMethodOpenQO qo = new JsfMethodOpenQO();
        qo.setInterfaceId(interfaceId);
        qo.setNameEn(methodName);
        final ReplyVO<List<JsfMethodOpenVO>> replyVo = jsfMockOpenAPI.queryMethods(qo);
        log.info("jsf.query_jsf_method:interfaceId={},methodName={},replyVo={}", interfaceId, methodName, JsonUtils.toJSONString(replyVo));
        if (isSuccess(replyVo)
                && !CollectionUtils.isEmpty(replyVo.getData())
                && replyVo.getData().get(0) != null
        ) {
            return replyVo.getData().get(0);
        }
        return null;
    }

    public Integer queryJsfMethodId(Integer interfaceId, String methodName) {
        final JsfMethodOpenVO vo = queryJsfMethod(interfaceId, methodName);
        if (vo != null) return vo.getId();
        return null;
    }

    public Integer addJsfMethod(InterfaceManage interfaceManage, MethodManage method, String relatedInterfaceId) {
        if (relatedInterfaceId == null) return null;
        Integer id = null;
        if (relatedInterfaceId != null) {
            id = queryJsfMethodId(Integer.valueOf(relatedInterfaceId), method.getMethodCode());
        }

        if (id != null) {
            //method.setRelatedId(id.longValue());
            //methodManageService.updateById(method);
            return id;
        }
        final UserInfoInSession user = UserSessionLocal.getUser();
        JsfMethodOpenAddDTO methodAddDto = new JsfMethodOpenAddDTO();
        methodAddDto.setInterfaceId(Integer.valueOf(relatedInterfaceId));
        methodAddDto.setNameCn(method.getName());
        methodAddDto.setNameEn(method.getMethodCode());
        methodAddDto.setCreatedTime(new Date());
        methodAddDto.setMethodSwitch(1); // 默认开启mock功能，也就是透传为false
        methodAddDto.setCreatedBy(user.getUserId());
        final ReplyVO<Integer> replyVO = jsfMockOpenAPI.addMethod(methodAddDto);
        if (!replyVO.getCode().equals("0")) {
            log.error("jsf.err_add_method:interfaceId={},methodId={},response={}", interfaceManage.getId(), method.getId(), JsonUtils.toJSONString(replyVO));
            throw new BizException("mock方法增加失败：" + replyVO.getMessage());
        }
        //method.setRelatedId(replyVO.getData().longValue());
        //methodManageService.updateById(method);
        return replyVO.getData();
    }

    /**
     * 更新mock模板数据。如果用户在接口定义里手动维护了mock模板数据，那么就不更新
     *
     * @param method
     * @param relatedId
     */

    public void updateHttpTemplate(MethodManage method, String relatedId) {
        HttpDataDto dto = new HttpDataDto();
        dto.setHttpId(Integer.valueOf(relatedId));
        // dto.setName(DEFAULT_MOCK_TEMPLATE);
        //dto.setEnabled(HTTP_TEMPLATE_DEFAULT_ENABLED);

        //dto.setCreatedBy(UserSessionLocal.getUser().getUserId());
        ReplyVO<List<HttpDataVO>> replyVO = httpMockJsf.queryHttpTemplateByParams(dto);
        if ("0".equals(replyVO.getCode())) {
            if (replyVO.getData().size() > 0) {
                HttpMethodModel methodModel = null;
                if (method.getContentObject() != null) {
                    methodModel = (HttpMethodModel) method.getContentObject();
                } else {
                    methodModel = JsonUtils.parse(method.getContent(), HttpMethodModel.class);
                }
                for (HttpDataVO datum : replyVO.getData()) {
                    if (DEFAULT_MOCK_TEMPLATE.equals(datum.getName())) {
                        if (ObjectHelper.isEmpty(methodModel.getOutput()) || ObjectHelper.isEmpty(methodModel.getOutput().getBody()))
                            return;
                        Object mockData = methodModel.getOutput().getBody().get(0).toExprValue(new ValueBuilderAcceptor() {
                            @Override
                            public Object afterSetValue(Object value, JsonType jsonType) {
                                if (!ObjectHelper.isEmpty(jsonType.getMock())) {
                                    return jsonType.getMock();
                                }
                                return value;
                            }
                        });
                        String resBody = datum.getResBody();
                        if (StringUtils.isBlank(resBody)) continue;
                        try {
                            Object resBodyRaw = JsonUtils.parse(resBody);
                            boolean hasUpdate = mergeMockDataTemplate(resBodyRaw, mockData);
                            if (hasUpdate) {
                                HttpDataUpdateDto dataUpdateDto = new HttpDataUpdateDto();
                                dataUpdateDto.setId(datum.getId());
                                dataUpdateDto.setHttpId(datum.getHttpId());

                                //dataUpdateDto.setHttpId(datum.getHttpId());
                                dataUpdateDto.setResBody(JsonUtils.toJSONString(resBodyRaw));

                                ReplyVO<HttpDataDto> httpDataDtoReplyVO = httpMockJsf.updateHttpTemplate(dataUpdateDto);
                                if (!isSuccess(httpDataDtoReplyVO)) {
                                    log.error("http.err_update_template:methodId={},response={}", method.getId(), JsonUtils.toJSONString(httpDataDtoReplyVO));
                                }


                            }
                        } catch (Exception e) {
                            log.error("http.err_update_template:methodId={},response={}", method.getId(), resBody, e);
                        }

                    }
                }
                return;
            }
           /* for (HttpDataVO item : replyVO.getData()) {
                httpMockJsf.deleteTemplateById(item.getId());
            }
*/

            addHttpTemplate(method, relatedId);
        }
    }

    /**
     * 把mock值重新填充到默认模板里
     *
     * @param resBody
     * @param mockTemplate
     * @return
     */
    private boolean mergeMockDataTemplate(Object resBody, Object mockTemplate) {

        if (resBody instanceof Map) {
            if (!(mockTemplate instanceof Map)) return false;
            Map<String, Object> resBodyMap = (Map<String, Object>) resBody;
            Map<String, Object> mockTemplateMap = (Map<String, Object>) mockTemplate;
            boolean hasUpdate = false;
            for (Map.Entry<String, Object> entry : resBodyMap.entrySet()) {
                Object val = mockTemplateMap.get(entry.getKey());
                if (entry.getValue() instanceof Map || entry.getValue() instanceof List) {
                    boolean childHasUpdate = mergeMockDataTemplate(entry.getValue(), val);
                    if (childHasUpdate) {
                        hasUpdate = true;
                    }
                    continue;
                }
                if (!ObjectHelper.isEmpty(val)) {
                    hasUpdate = true;
                    entry.setValue(val);
                }
            }
            return hasUpdate;
        } else if (resBody instanceof List) {
            boolean hasUpdate = false;
            if (ObjectHelper.isEmpty(mockTemplate)) return false;
            if (!(mockTemplate instanceof List)) return false;
            List<Object> resBodyList = (List<Object>) resBody;
            if (resBodyList.size() != 1) return false;// 只处理listMap类型的
            for (Object o : resBodyList) {
                boolean childHasUpdate = mergeMockDataTemplate(o, mockTemplate);
                if (childHasUpdate) {
                    hasUpdate = true;
                }
            }
            return hasUpdate;
        } else {
            return false;
        }
    }

    public void updateHttpDefaultTemplate(MethodManage methodManage, String relatedId, String body) {
        HttpDataDto dto = new HttpDataDto();
        dto.setHttpId(Integer.valueOf(relatedId));
        // dto.setName(DEFAULT_MOCK_TEMPLATE);
        //dto.setEnabled(HTTP_TEMPLATE_DEFAULT_ENABLED);

        //dto.setCreatedBy(UserSessionLocal.getUser().getUserId());
        ReplyVO<List<HttpDataVO>> replyVO = httpMockJsf.queryHttpTemplateByParams(dto);
        if ("0".equals(replyVO.getCode())) {
            /*if(replyVO.getData().size() > 0){
                return;
            }*/
            for (HttpDataVO item : replyVO.getData()) {
                if ("1".equals(item.getResCode())) { // 清理默认模板
                    httpMockJsf.deleteTemplateById(item.getId());
                }
            }
            addHttpTemplate(methodManage, relatedId, body);
        }
    }

    public void addJsfDebugTemplate(InterfaceManage interfaceManage, MethodManage methodManage, List<JsfDebugData> jsfDebugDtos, Integer jsfMethodId) {
        final JsfStepMetadata jsfStepMetadata = JsonUtils.parse(methodManage.getContent(), JsfStepMetadata.class);
        List<JsfDataOpenAddDTO> dtos = new ArrayList<>();
        for (JsfDebugData jsfDebugDto : jsfDebugDtos) {
            JsfDataOpenAddDTO dto = new JsfDataOpenAddDTO();
            dto.setMethodId(jsfMethodId);
            dto.setInterfaceName(interfaceManage.getServiceCode());
            if (StringUtils.isNotEmpty(jsfDebugDto.getDesc())) {
                dto.setName(jsfDebugDto.getDesc());
            } else {
                dto.setName("快捷调用模板");
            }
            dto.setAlias(DEFAULT_ALIAS);
            dto.setDataType(0);// 默认模板
            dto.setRegex(0);// 不开启正则
            dto.setCreatedBy(UserSessionLocal.getUser().getUserId());
            dto.setEnabled(0);

            if (jsfDebugDto.getOutput() != null) {
                try {
                    JsfOutputExt output = JsonUtils.parse(JsonUtils.toJSONString(jsfDebugDto.getOutput()), JsfOutputExt.class);
                    dto.setOutput(JsonUtils.toJSONString(output.getBody()));
                } catch (Exception e) {
                    log.error("jsf.err_add_debug_template:interfaceId={},methodId={},response={}", interfaceManage.getId(), methodManage.getId(), JsonUtils.toJSONString(jsfDebugDto));
                }

            }
            if (jsfDebugDto.getInput().getInputData() != null) {
                String data = JsonUtils.toJSONString(jsfDebugDto.getInput().getInputData());
                if (data.startsWith("[")) {
                    data = data.substring(1, data.length() - 1);
                }
                dto.setInput(data);
            } else {
                List paramValue = new ArrayList();
                Map<String, Object> extArgs = new HashMap<>();
                extArgs.put("input", new HashMap<>());
                ParamMappingContext paramMappingContext = new ParamMappingContext(new StepContext(), extArgs);
                Map<String, Object> inputValues = utils.getJsonInputValue(jsfDebugDto.getInput().getInput(), paramMappingContext);
                for (JsonType jsonType : jsfDebugDto.getInput().getInput()) {
                    Object value = inputValues.get(jsonType.getName());
                    Object convertValue = JsfParamConverterRegistry.convertValue(jsonType, value);
                    paramValue.add(convertValue);

                }
                String data = JsonUtils.toJSONString(paramValue);
                dto.setInput(data.substring(1, data.length() - 1));
            }
            dtos.add(dto);
            final ReplyVO<Void> replyVo = jsfMockOpenAPI.batchInsertTemplate(dtos);
            log.info("mock.add_jsf_template:interfaceId={},methodId={},dto={},replyVO={}", interfaceManage.getId(), methodManage.getId(), JsonUtils.toJSONString(dto), JsonUtils.toJSONString(replyVo));

            if (!isSuccess(replyVo)) {
                log.info("mock.err_add_jsf_template:interfaceId={},methodId={},dto={},replyVO={}", interfaceManage.getId(), methodManage.getId(), JsonUtils.toJSONString(dto), JsonUtils.toJSONString(replyVo));

            }
            // 【指标度量】快捷调用一键mock模版（jsf）
            measureDataService.saveQuickCallMockTempLog(MeasureDataEnum.QUICK_CALL_MOCK_JSF_TEMPLATE.getCode(), String.valueOf(jsfMethodId));
        }


    }

    public void addHttpDebugData(MethodManage methodManage, HttpDebugDataDto dtoData, String relatedId) {
        HttpDataAddDto template = new HttpDataAddDto();
        template.setHttpId(Integer.valueOf(relatedId));
        if (org.apache.commons.lang3.StringUtils.isEmpty(dtoData.getDesc())) {
            template.setName("快捷调用记录");
        } else {
            template.setName(dtoData.getDesc());
        }
        template.setEnabled(HTTP_TEMPLATE_DEFAULT_ENABLED);

        template.setRemark("根据快捷调用生成的模板");


        template.setCreatedBy(UserSessionLocal.getUser().getUserId());
        template.setCreatedTime(new Date());
        final HttpMethodModel methodModel = JsonUtils.parse(methodManage.getContent(), HttpMethodModel.class);
        Map<String, Object> reqBody = new HashMap<>();
        if (dtoData.getInput().getHeaders() != null) {
            dtoData.getInput().getHeaders().remove("cookie");
            reqBody.put("headers", dtoData.getInput().getHeaders());
        }

        if (dtoData.getInput().getParams() != null) {
            reqBody.put("params", dtoData.getInput().getParams());

        } else {
            reqBody.put("params", new HashMap<>());

        }


        ReqType reqType = ReqType.json;
        if (!StringUtils.isBlank(methodModel.getInput().getReqType())) {
            reqType = ReqType.valueOf(methodModel.getInput().getReqType());
        }
        if (!CollectionUtils.isEmpty(methodModel.getInput().getBody())) {
            if (ReqType.json.equals(reqType)) {
                reqBody.put("body", dtoData.getInput().getBody());
            } else {
                reqBody.put("body", dtoData.getInput().getBody());
            }
        }

        reqBody.put("contentType", reqType.getContentType());
        template.setReqBody(JsonUtils.toJSONString(reqBody));

        template.setResCode("0"); // 是否默认模板
        template.setType(1);// 0-默认模板 1-普通模板

        if (dtoData.getOutput() != null) {
            template.setResBody(JsonUtils.toJSONString(dtoData.getOutput().getBody()));
        }


        final ReplyVO<HttpDataDto> replyVO = httpMockJsf.addHttpTemplate(template);
        if (!"0".equals(replyVO.getCode())) {
            throw new BizException("mock模板增加失败:" + replyVO.getMessage());
        }
        // 【指标度量】快捷调用一键mock模版（http）
        measureDataService.saveQuickCallMockTempLog(MeasureDataEnum.QUICK_CALL_MOCK_HTTP_TEMPLATE.getCode(), relatedId);
    }

    public void addHttpTemplate(MethodManage methodManage, String relatedId) {
        addHttpTemplate(methodManage, relatedId, null);
    }

    public void addHttpTemplate(MethodManage methodManage, String relatedId, String resBody) {
        HttpDataAddDto template = new HttpDataAddDto();
        template.setHttpId(Integer.valueOf(relatedId));
        template.setName(DEFAULT_MOCK_TEMPLATE);
        template.setEnabled(HTTP_TEMPLATE_DEFAULT_ENABLED);
        template.setRemark("生成的默认模板,请复制后使用");
        template.setCreatedBy(UserSessionLocal.getUser().getUserId());
        template.setCreatedTime(new Date());
        final HttpMethodModel methodModel = JsonUtils.parse(methodManage.getContent(), HttpMethodModel.class);
        HttpDemoValue demoValue = mockDataBuildService.buildHttpDemoValue(methodModel, true);

        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put("headers", demoValue.getInputHeaders());

        reqBody.put("params", demoValue.getInputParams());


        ReqType reqType = ReqType.json;
        if (!StringUtils.isBlank(methodModel.getInput().getReqType())) {
            reqType = ReqType.valueOf(methodModel.getInput().getReqType());
        }
        if (!CollectionUtils.isEmpty(methodModel.getInput().getBody())) {
            if (ReqType.json.equals(reqType)) {
                reqBody.put("body", demoValue.getInputBody());
            } else {
                reqBody.put("body", demoValue.getInputBody());
            }
        }

        reqBody.put("contentType", reqType.getContentType());
        template.setReqBody(JsonUtils.toJSONString(reqBody));

        template.setResCode("1"); // 是否默认模板
        template.setType(0);// 0-默认模板 1-普通模板

        if (resBody != null) {
            template.setResBody(resBody);
        } else {
            if (methodModel.getOutput() != null && !CollectionUtils.isEmpty(methodModel.getOutput().getBody())) {
                JsonType rootJsonType = methodModel.getOutput().getBody().get(0);
                template.setResBody(JsonUtils.toJSONString(demoValue.getOutputBody()));
            }
        }


        final ReplyVO<HttpDataDto> replyVO = httpMockJsf.addHttpTemplate(template);
        if (!"0".equals(replyVO.getCode())) {
            throw new BizException("mock模板增加失败:" + replyVO.getMessage());
        }
        // 【指标度量】mock(http默认模版)
        measureDataService.saveMockTemplateLog(MeasureDataEnum.MOCK_HTTP_DEFAULT_TEMPLATE.getCode(), relatedId);
    }

    Map<String, Object> buildInput(List<? extends JsonType> jsonTypes) {
        if (jsonTypes == null) return null;
        Map<String, Object> reqHeaders = utils.buildInput(jsonTypes);
        return reqHeaders;
    }

    public void addJsfTemplate(InterfaceManage interfaceManage, MethodManage methodManage, Integer jsfMethodId) {
        final JsfStepMetadata jsfStepMetadata = JsonUtils.parse(methodManage.getContent(), JsfStepMetadata.class);
        JsfDemoValue jsfDemoValue = mockDataBuildService.buildJsfDemoValue(jsfStepMetadata, true);
        JsfDataOpenAddDTO dto = new JsfDataOpenAddDTO();
        dto.setMethodId(jsfMethodId);
        dto.setInterfaceName(interfaceManage.getServiceCode());
        dto.setName(DEFAULT_MOCK_TEMPLATE);
        dto.setAlias(DEFAULT_ALIAS);
        dto.setDataType(0);// 默认模板
        dto.setRegex(0);// 不开启正则
        dto.setCreatedBy(UserSessionLocal.getUser().getUserId());
        dto.setEnabled(0);

        if (jsfStepMetadata.getOutput() != null) {

            dto.setOutput(JsonUtils.toJSONString(jsfDemoValue.getOutputMockValue()));
        }
        List<Object> inputMockValue = jsfDemoValue.getInputMockValue();
        ReqDemoBuildUtils.removeClassProp(inputMockValue);
        String inputStr = JsonUtils.toJSONString(inputMockValue);
        if (inputStr.startsWith("[") && inputStr.endsWith("]")) {
            inputStr = inputStr.substring(1, inputStr.length() - 1).trim();
        }
        dto.setInput(inputStr);
        final ReplyVO<Void> replyVo = jsfMockOpenAPI.batchInsertTemplate(Collections.singletonList(dto));
        log.info("mock.add_jsf_template:interfaceId={},methodId={},dto={},replyVO={}", interfaceManage.getId(), methodManage.getId(), JsonUtils.toJSONString(dto), JsonUtils.toJSONString(replyVo));

        if (!isSuccess(replyVo)) {
            log.info("mock.err_add_jsf_template:interfaceId={},methodId={},dto={},replyVO={}", interfaceManage.getId(), methodManage.getId(), JsonUtils.toJSONString(dto), JsonUtils.toJSONString(replyVo));

        }
        // 【指标度量】mock(jsf默认模版)
        measureDataService.saveMockTemplateLog(MeasureDataEnum.MOCK_JSF_DEFAULT_TEMPLATE.getCode(), String.valueOf(jsfMethodId));

    }

    Object buildDemoValue(JsonType jsonType) {
        if (jsonType instanceof SimpleJsonType) {
            final JsfParamConverter converter = JsfParamConverterRegistry.getConverter(jsonType);
            if (converter instanceof PrimitiveParamConverter) {
                try {
                    return converter.getDemoValue(JsfParamConverterRegistry.getSimpleTypeClass(jsonType));
                } catch (Exception e) {
                    log.error("jsf.err_build_demo_value:class={}", jsonType.getClassName(), e);
                }
            }
        }
        return null;
    }

    private Integer getDefaultJsfTemplate(Integer methodId) {
        JsfTemplateOpenQO dto = new JsfTemplateOpenQO();
        dto.setMethodId(methodId);
        final ReplyVO<List<JsfTemplateOpenVO>> replyVo = jsfMockOpenAPI.queryTemplates(dto);
        if (!isSuccess(replyVo)) {
            return null;
        }
        final List<JsfTemplateOpenVO> data = replyVo.getData();
        for (JsfTemplateOpenVO item : data) {
            if (DEFAULT_MOCK_TEMPLATE.equals(item.getName())) {
                return item.getId();
            }
        }
        return null;
    }

    private List<JsfTemplateOpenVO> getJsfTemplates(Integer methodId) {
        JsfTemplateOpenQO dto = new JsfTemplateOpenQO();
        dto.setMethodId(methodId);
        final ReplyVO<List<JsfTemplateOpenVO>> replyVo = jsfMockOpenAPI.queryTemplates(dto);
        if (!isSuccess(replyVo)) {
            return null;
        }
        final List<JsfTemplateOpenVO> data = replyVo.getData();
        return data;
    }

    public void updateJsfTemplate(InterfaceManage interfaceManage, MethodManage methodManage, Integer jsfMethodId) {
        final Integer defaultTemplateId = getDefaultJsfTemplate(jsfMethodId);
        if (defaultTemplateId == null) {
            addJsfTemplate(interfaceManage, methodManage, jsfMethodId);
        } else {
            JsfDataOpenDto dto = new JsfDataOpenDto();
            dto.setDataId(defaultTemplateId);
            dto.setOperator(UserSessionLocal.getUser().getUserId());
            JsfUpdateModel jsfUpdateModel = new JsfUpdateModel();
            final JsfStepMetadata jsfStepMetadata = JsonUtils.parse(methodManage.getContent(), JsfStepMetadata.class);

            if (jsfStepMetadata.getOutput() != null) {
                jsfUpdateModel.setDataOutput(getJsfOutputDemoValue(jsfStepMetadata));
            }
            jsfUpdateModel.setDataInput(getJsfInputDemoValue(jsfStepMetadata));

            dto.setJsfUpdateModel(jsfUpdateModel);
            final ReplyVO<Void> replyVO = jsfMockOpenAPI.changeTemplate(dto);
            log.info("jsf.change_template:dto={},result={}", JsonUtils.toJSONString(dto), JsonUtils.toJSONString(replyVO));
        }


    }

    private String getJsfOutputDemoValue(JsfStepMetadata jsfStepMetadata) {
        Object value = jsfStepMetadata.getOutput().toExprValue(new ValueBuilderAcceptor() {
            @Override
            public Object afterSetValue(Object value, JsonType jsonType) {
                final Object demoValue = buildDemoValue(jsonType);
                if (demoValue != null) return demoValue;
                return value;
            }
        });
        return JsonUtils.toJSONString(JsfParamConverterRegistry.convertValue(jsfStepMetadata.getOutput(), value));
    }

    private String getJsfInputDemoValue(JsfStepMetadata jsfStepMetadata) {
        List paramValue = new ArrayList();
        for (JsonType jsonType : jsfStepMetadata.getInput()) {
            final Object value = jsonType.toExprValue(new ValueBuilderAcceptor() {
                @Override
                public Object afterSetValue(Object value, JsonType jsonType) {
                    final Object demoValue = buildDemoValue(jsonType);
                    if (demoValue != null) return demoValue;
                    return value;
                }
            });
            paramValue.add(JsfParamConverterRegistry.convertValue(jsonType, value));
        }
        removeClassProp(paramValue);

        String ret = JsonUtils.toJSONString(paramValue);
        // 去掉前后2个参数
        return ret.substring(1, ret.length() - 1);
    }

    /*
        移除map类型里的class值
     */
    private void removeClassProp(Object value) {
        if (value == null) return;
        if (value instanceof Map) {
            ((Map) (value)).remove("class");
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                removeClassProp(entry.getValue());
            }
        } else if (value instanceof List) {
            for (Object val : (List) value) {
                removeClassProp(val);
            }
        }
    }

    private String getMavenInfo(String mavenPath) {
        String pomInfo = " <dependency>\n" +
                "            <groupId>%s</groupId>\n" +
                "            <artifactId>%s</artifactId>\n" +
                "            <version>%s</version>\n" +
                "        </dependency>";
        String[] paths = StringUtils.split(mavenPath, ':');
        return String.format(pomInfo, paths[0], paths[1], paths[2]);
    }

    /**
     * 将接口数据以及方法数据同步到easymock
     * 添加步骤
     * 针对http接口： 添加http方法 -> 添加http模板
     * 针对jsf接口： 添加jsf接口-> 开启jsf mock (openOrCloseInterface) -> 添加jsf模板
     *
     * @return
     */
    public SyncMockDataResult syncMockData(InterfaceManage interfaceManage, MethodManage methodManage) {
        if (InterfaceTypeEnum.HTTP.getCode() == interfaceManage.getType() || InterfaceTypeEnum.EXTENSION_POINT.getCode() == interfaceManage.getType()) {
            Integer relatedHttpMethodId = addHttpMethod(interfaceManage, methodManage);
            updateHttpTemplate(methodManage, relatedHttpMethodId + "");
            return new SyncMockDataResult(relatedHttpMethodId, null);
        } else if (InterfaceTypeEnum.JSF.getCode() == interfaceManage.getType()) {
            String error = null;
            JsfInterfaceOpenVO jsfInterfaceVo = addOrUpdateJsfInterface(interfaceManage, false); // 这里不再更新接口信息
            //JsfInterfaceOpenVO jsfInterfaceVo = easyMockRemoteService.queryJsfInterface(interfaceManage);
            if (jsfInterfaceVo.getMockSwitch() == null
                    || jsfInterfaceVo.getMockSwitch() == 0
            ) { // 校验接口是否开启mock了
                error = openOrCloseInterface(interfaceManage, true);
            }

            /*if(error != null){
                return new SyncMockDataResult(null,"开启jsf mock失败："+error);
            }*/

            final Integer jsfMethodId = addJsfMethod(interfaceManage, methodManage, jsfInterfaceVo.getId() + "");
            updateJsfTemplate(interfaceManage, methodManage, jsfMethodId);
            return new SyncMockDataResult(jsfMethodId, error);
        }
        return null;
    }


    public boolean methodIsSwitch(String methodId) {
        MethodManage methodManage = methodManageService.getById(methodId);
        Guard.notEmpty(methodManage, "无效的方法id");
        InterfaceManage interfaceManage = interfaceManageService.getById(methodManage.getInterfaceId());
        Guard.notEmpty(interfaceManage, "无效的接口id");
        Integer relatedInterfaceId = queryJsfInterfaceById(interfaceManage);
        final JsfMethodOpenVO vo = queryJsfMethod(relatedInterfaceId, methodManage.getMethodCode());
        if (vo == null) return false;
        final Integer methodSwitch = vo.getMethodSwitch();
        if (methodSwitch == 1) { // 开启了mock,透传为false
            return false;
        } else {
            return true;
        }
    }

    public JsfInterfaceOpenVO queryJsfMockInterfaceByInterfaceId(String interfaceId) {
        InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
        Guard.notEmpty(interfaceManage, "无效的接口id");

        JsfInterfaceOpenVO jsfInterfaceOpenVO = queryJsfInterface(interfaceManage);
        if (jsfInterfaceOpenVO == null) {
            jsfInterfaceOpenVO = new JsfInterfaceOpenVO();
        } else {
            if (jsfInterfaceOpenVO.getDeliverFlag() != null
            ) { // 透传了
                if (0 == jsfInterfaceOpenVO.getDeliverFlag()) { // 未透传
                    jsfInterfaceOpenVO.setDeliverToken(null);
                    jsfInterfaceOpenVO.setDeliverAlias(null);
                }
            }
        }
        return jsfInterfaceOpenVO;
    }

    private InterfaceManage getMethodByPathAndInterfaceCode(String url, String interfaceCode) {

     /*   {
            LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
            lqw.eq(InterfaceManage::getYn,1);
            lqw.eq(InterfaceManage::getServiceCode,interfaceCode);
            List<InterfaceManage> interfaceManages = interfaceManageService.list(lqw);
            if(interfaceManages.size() == 1){
                return interfaceManages.get(0);
            }
        }*/
        {
            LambdaQueryWrapper<MethodManage> methodLqw = new LambdaQueryWrapper<>();
            methodLqw.select(MethodManage::getId, MethodManage::getPath, MethodManage::getInterfaceId, MethodManage::getYn);
            methodLqw.eq(MethodManage::getYn, 1);
            methodLqw.eq(MethodManage::getPath, url);
            methodLqw.eq(MethodManage::getType, InterfaceTypeEnum.HTTP.getCode());
            List<MethodManage> methods = methodManageService.list(methodLqw);
            if (methods.isEmpty()) return null;

            List<Long> interfaceIds = methods.stream().map(method -> method.getInterfaceId()).collect(Collectors.toList());
            LambdaQueryWrapper<InterfaceManage> interfaceLqw = new LambdaQueryWrapper<>();
            interfaceLqw.select(InterfaceManage::getId, InterfaceManage::getServiceCode, InterfaceManage::getYn, InterfaceManage::getAppId);
            interfaceLqw.eq(InterfaceManage::getServiceCode, interfaceCode);
            interfaceLqw.in(InterfaceManage::getId, interfaceIds);
            interfaceLqw.eq(InterfaceManage::getYn, 1);
            List<InterfaceManage> list = interfaceManageService.list(interfaceLqw);


            if (list.size() == 0) {
                return null;
            } else if (list.size() > 1) {
                log.error("mock.err_found_found_multi_interface_code:interfaceCode={},url={}", interfaceCode, url);
                return list.get(0);
            }
            return list.get(0);
        }
    }

    public void moveMockDataToNewPlatform() {
        HttpInterfaceDto dto = new HttpInterfaceDto();
        dto.setPageNo(1);
        dto.setPageSize(3000);
        dto.setPlatform(2);
        ReplyVO<List<HttpInterfaceVO>> replyVo = httpMockJsf.getInterfacesByParams(dto);
        if (isSuccess(replyVo)) {
            int i = 0;
            for (HttpInterfaceVO interfaceVo : replyVo.getData()) {
                String[] paths = interfaceVo.getUrl().split("/");
                log.info("mock.found_http_interface:----------------------i={}", i);
                i++;
                if (paths.length < 4) {
                    log.error("mock.err_fond_invalid_interface_manageLurl={}", interfaceVo.getUrl());
                    continue;
                }
                String interfaceName = paths[2];
                String methodPath = interfaceVo.getUrl().substring((METHOD_PREFIX + "/").length() + interfaceName.length());
                InterfaceManage interfaceManage = getMethodByPathAndInterfaceCode(methodPath, interfaceName);
                if (interfaceManage == null) {
                    log.error("mock.err_fond_invalid_interface_manageLurl={}", interfaceVo.getUrl());
                    continue;
                }
                Long appId = interfaceManage.getAppId();
                if (appId == null) {
                    log.error("mock.err_fond_invalid_app_id:interfaceId={}", interfaceManage.getId());
                    continue;
                }
                AppInfo appInfo = appInfoService.getById(appId);
                if (appInfo == null) {
                    log.error("mock.err_fond_invalid_app_id:interfaceId={}", interfaceManage.getId());
                    continue;
                }
                String newPath = NEW_METHOD_PREFIX + "/" + appInfo.getAppCode() + methodPath;
                long start = System.currentTimeMillis();
                try {
                    log.info("mock.begin_copy_template:newPath={}", newPath);
                    copyConfigAndTemplate(interfaceVo, newPath);
                } catch (Exception e) {
                    log.error("mock.err_copy_template:newPath={},cost={}", newPath, System.currentTimeMillis() - start, e);
                } finally {
                    log.info("mock.end_copy_template:newPath={},cost={}", newPath, System.currentTimeMillis() - start);
                }

                //break;
            }
            log.info("mock.end_copy_template:total={}", replyVo.getData().size());
        }
    }

    private void copyConfigAndTemplate(HttpInterfaceVO old, String newPath) {
        HttpInterfaceVO newVo = null;
        {
            ReplyVO<HttpInterfaceVO> replyVo = httpMockJsf.getInterfaceByUrlAndMethod(newPath, old.getMethod());
            if (isSuccess(replyVo) && replyVo.getData() != null) {
                newVo = replyVo.getData();
            }
        }
        if (newVo == null) {
            HttpInterfaceAddDTO httpDto = new HttpInterfaceAddDTO();
            httpDto.setCreatedBy(old.getCreatedBy());
            httpDto.setCreatedTme(old.getCreatedTme());
            httpDto.setMethod(old.getMethod());
            httpDto.setPlatform(2);
            httpDto.setName(old.getName());
            httpDto.setMockSwitch(1); // mock切换
            httpDto.setUrl(newPath);


            ReplyVO<HttpInterfaceVO> replyVo = httpMockJsf.addHttpInterface(httpDto);
            if (!isSuccess(replyVo)) { // 成功了
                log.error("easymock.err_add_http_interface:url={},replyVO={}", newPath, JsonUtils.toJSONString(replyVo));
                throw new BizException("mock数据增加失败：" + replyVo.getMessage());

            }
            replyVo = httpMockJsf.getInterfaceByUrlAndMethod(newPath, old.getMethod());
            if (isSuccess(replyVo) && replyVo.getData() != null) {
                newVo = replyVo.getData();
            }

        }
        try {
            copyTemplates(old, newVo);
        } catch (Exception e) {
            log.error("easymock.err_copy_mock_template", e);
        }


    }

    private void copyTemplates(HttpInterfaceVO oldVo, HttpInterfaceVO newVo) {
        HttpDataDto dto = new HttpDataDto();
        dto.setHttpId(oldVo.getId());

        ReplyVO<List<HttpDataVO>> replyVO = httpMockJsf.queryHttpTemplateByParams(dto);
        if ("0".equals(replyVO.getCode())) {

            HttpDataDto newDto = new HttpDataDto();
            newDto.setHttpId(newVo.getId());
            ReplyVO<List<HttpDataVO>> newReplyListVo = httpMockJsf.queryHttpTemplateByParams(newDto);
            if (!ObjectHelper.isEmpty(newReplyListVo.getData())) {
                if (newReplyListVo.getData().size() > 10) {
                    throw new BizException("query data error:" + JsonUtils.toJSONString(newReplyListVo));
                }
                for (HttpDataVO datum : newReplyListVo.getData()) {
                    if (!newVo.getId().equals(datum.getHttpId())) {
                        throw new BizException("query data error:" + JsonUtils.toJSONString(newReplyListVo));
                    }
                }
                List<Integer> ids = newReplyListVo.getData().stream().map(item -> item.getId()).collect(Collectors.toList());
                ReplyVO<Void> deleteTemplateReplyVo = httpMockJsf.deleteTemplateByIds(ids);
                if (!isSuccess(deleteTemplateReplyVo)) {
                    log.info("easymock.err_delete_template:ids={},replyVo={}", ids, JsonUtils.toJSONString(deleteTemplateReplyVo));
                }
            }


            for (HttpDataVO oldTemplate : replyVO.getData()) {
                HttpDataAddDto template = new HttpDataAddDto();
                template.setHttpId(newVo.getId());
                template.setName(oldTemplate.getName());
                template.setEnabled(oldTemplate.getEnabled());
                template.setRemark(oldTemplate.getRemark());
                template.setCreatedBy(oldTemplate.getCreatedBy());
                template.setCreatedTime(oldTemplate.getCreatedTime());
                template.setReqBody(oldTemplate.getReqBody());
                template.setResCode(oldTemplate.getResCode()); // 是否默认模板
                template.setType(oldTemplate.getType());// 0-默认模板 1-普通模板
                template.setResBody(oldTemplate.getResBody());

                final ReplyVO<HttpDataDto> addTemplateVo = httpMockJsf.addHttpTemplate(template);
                if (!"0".equals(addTemplateVo.getCode())) {
                    throw new BizException("mock模板增加失败:" + replyVO.getMessage());
                }
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class SyncMockDataResult {
        Integer methodId;
        String warnInfo;

    }
}
