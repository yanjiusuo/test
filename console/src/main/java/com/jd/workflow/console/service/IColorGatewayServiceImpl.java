package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.up.portal.login.interceptor.UpLoginContextHelper;
import com.jd.workflow.console.dao.mapper.IColorGatewayMapper;
import com.jd.workflow.console.dto.FilterParam;
import com.jd.workflow.console.entity.ColorGatewayParam;
import com.jd.workflow.console.service.color.ColorApiParam;
import com.jd.workflow.console.service.color.ColorApiServiceImpl;
import com.jd.workflow.server.dto.color.ColorApiParamDto;
import com.jd.workflow.soap.common.method.ColorGatewayParamDto;
import com.jd.workflow.soap.common.xml.ColorGatewayEnumDTO;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.camel.support.ObjectHelper.compare;

@Slf4j
@Service
public class IColorGatewayServiceImpl extends ServiceImpl<IColorGatewayMapper, ColorGatewayParam> {

    @Resource
    ColorApiServiceImpl colorApiService;

    /**
     * 网关参数插入
     * @param param
     */
    public void insertParam( ColorGatewayParam param){
        baseMapper.insert(param);
    }

    /**
     * 网关参数更新
     * @param entitys
     * @param methodId
     * @return
     */
    public Integer updateColorParam(List<ColorGatewayParam> entitys, String methodId) {
        LambdaQueryWrapper<ColorGatewayParam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ColorGatewayParam::getMethodId, methodId);
        wrapper.eq(ColorGatewayParam::getYn, 1);
        Integer delNum = baseMapper.delete(wrapper);
        log.info("删除color网关 methodId={}参数数量delNum{}", methodId, delNum);
        int i = 0;
        for (ColorGatewayParam entity : entitys) {
            entity.setCreator("Color网关-" + entity.getCreator());
            entity.setDefaultShow(getDefaultShow(entity.getName()));
            try {
                baseMapper.insert(entity);
                i++;
            } catch (Exception e) {
                log.error("更新color网关参数信息失败methodId{}", methodId, e);
            }
        }
        return i;
    }

    /**
     * 默认展示的参数
     * @param name
     * @return
     */
    public Integer getDefaultShow(String name) {
        String[] defaultParams = {"Content-Type", "Cookie", "Referer", "User-Agent", "appid", "body", "client", "clientVersion", "functionId", "loginType", "sign", "t", "uuid"};
        List<String> params = Arrays.asList(defaultParams);
        if (params.contains(name)) {
            return 1;
        }
        return 0;
    }


    /**
     * 查询网关参数
     * @param type  1-requestHeader 2-requestParam 3-responseHeader
     * @return
     */
    public List<JsonType> queryColorGateParam(Integer type, FilterParam filterParam) {
        List<ColorGatewayParam> pro=queryColorParam(type,null,"pro",filterParam);
        List<JsonType> res = new ArrayList<JsonType>();
        for (ColorGatewayParam colorGatewayParam : pro) {
            colorGatewayParam.setDefaultShow(getDefaultShow(colorGatewayParam.getName()));
//            baseMapper.updateById(colorGatewayParam);
        }
        if (CollectionUtils.isNotEmpty(pro)) {
            res = pro.stream().map(item -> parameterToJsonType(item,"pro")).collect(Collectors.toList());
        }
        return res;
    }

    /**
     * 根据functionId查询网关参数
     * @param functionId
     * @param filterParam 1 网关生成  state=2 color客户端必传信息
     * @return
     */
    public List<JsonType> querytParamByFunctionId(String functionId, FilterParam filterParam) {
        if (StringUtils.isEmpty(functionId)) {
            return null;
        }
        //source 1-网关生成 2-客户端传递
        List<ColorGatewayParam> pro=queryColorParam(null,functionId,"pro",filterParam);
        List<ColorGatewayParam> pre=queryColorParam(null,functionId,"pre",filterParam);
        if (CollectionUtils.isEmpty(pro)&&CollectionUtils.isEmpty(pre)) {
            return null;
        }
        return compareZoneParam(pro,pre);
    }

    /**
     * 比较正式环境与预发环境参数差异
     * @param pro
     * @param pre
     */
    private List<JsonType> compareZoneParam(List<ColorGatewayParam> pro, List<ColorGatewayParam> pre) {

        if(CollectionUtils.isNotEmpty(pro)&&CollectionUtils.isEmpty(pre)){
            return  pro.stream().map(item -> parameterToJsonType(item,"pro")).collect(Collectors.toList());
        }
        if(CollectionUtils.isEmpty(pro)&&CollectionUtils.isNotEmpty(pre)){
            return  pre.stream().map(item -> parameterToJsonType(item,"pre")).collect(Collectors.toList());
        }

        List<JsonType> proParams=pro.stream().map(item -> parameterToJsonType(item,"pro")).collect(Collectors.toList());
        //如果是空，按照0处理
        Map<String, Integer> preData = pre.stream().collect(Collectors.toMap(ColorGatewayParam::getName,(i)->{
           return i.getIsTransparent()==null?0:i.getIsTransparent();
        }));
        for (JsonType currParam : proParams) {
            if (preData.keySet().contains(currParam.getName())) {
                if (!preData.get(currParam.getName()).equals(currParam.getIsTransparent().get("pro").getValue())) {
                    ColorGatewayEnumDTO dto = ColorGatewayEnumDTO.getSimpleYesOrNo(preData.get(currParam.getName()));
                    currParam.getIsTransparent().put("pre", dto);
                }
            }else {
                //预发环境没有此字段
                currParam.setDataZone(1);
            }
        }
        // 填充预发环境独有的字段
//        Map<String, Integer> proData = res.stream().collect(Collectors.toMap(JsonType::getName, (h) -> {
//            if(null== h.getIsTransparent()||null==h.getIsTransparent().get("pro")||h.getIsTransparent().get("pro").getValue()==null){
//                return 0;
//            }
//            return h.getIsTransparent().get("pro").getValue();
//        }));
        List<String> names=proParams.stream().map(JsonType::getName).collect(Collectors.toList());
        for (ColorGatewayParam preParam : pre) {
            if (!names.contains(preParam.getName())) {
                JsonType type = parameterToJsonType(preParam, "pre");
                //预发关注,线上环境没有此字段
                type.setDataZone(2);
                proParams.add(type);
            }
        }
        return proParams;
    }

    public List<ColorGatewayParam> queryColorParam(Integer type,String functionId,String zone,FilterParam filterParam ) {

        List<ColorGatewayParam> params=new ArrayList<>();
        if (StringUtils.isNotEmpty(functionId)) {
            String zoneCluster ="";
            if ("pre".equals(zone)) {
                zoneCluster="beta-api.m.jd.com";
            }
            //zoneCluster不传 默认查线上 默认值：api.m.jd.com  扩展可以通过zone字段进行精确查询
            //查询网关参数+自定义参数
            List<ColorApiParam> colorResult = colorApiService.queryColorInfoByFunctionId(functionId, UpLoginContextHelper.getUserPin(),zoneCluster);
            params = convertParam(colorResult, filterParam);
        }else{
            //查询默认网关参数
            LambdaQueryWrapper<ColorGatewayParam> queryWrapper = new LambdaQueryWrapper<ColorGatewayParam>();
            queryWrapper.eq(null != type, ColorGatewayParam::getType, type);
            queryWrapper.eq(ColorGatewayParam::getYn, 1);
            if (null!=filterParam) {
                queryWrapper.eq(filterParam.getSource()!=null,ColorGatewayParam::getSource, filterParam.getSource());
                queryWrapper.eq(filterParam.getIsNecessary()!=null,ColorGatewayParam::getIsAppNecessary, filterParam.getIsNecessary());
            }
            params = baseMapper.selectList(queryWrapper);
        }

        return params;
    }


    private List<ColorGatewayParam> convertParam(List<ColorApiParam> colorResult,FilterParam filter) {
        List<ColorGatewayParam> params = new ArrayList<>();
        if (CollectionUtils.isEmpty(colorResult) || CollectionUtils.isEmpty(colorResult.get(0).getParams())) {
            return null;
        }
        //color菜单参数过滤
        for (ColorApiParam.GateWayParam param : colorResult.get(0).getParams()) {
            if (CollectionUtils.isNotEmpty(param.getGatewayParam())) {
                for (ColorApiParamDto colorGateWay : param.getGatewayParam()) {
                    if(colorGateWay.getType()!=3){
                        if(null!=filter&&filter.getSource()!=null&&filter.getSource()!=colorGateWay.getSource()){
                            continue;
                        }
                        if(null!=filter&&filter.getIsNecessary()!=null&&filter.getIsNecessary()!=colorGateWay.getIsAppNecessary()){
                            continue;
                        }
                    }
                    ColorGatewayParam gateway = new ColorGatewayParam();
                    BeanUtils.copyProperties(colorGateWay, gateway);
                    gateway.setDefaultShow(getDefaultShow(gateway.getName()));
                    params.add(gateway);
                }
            }
            if (CollectionUtils.isNotEmpty(param.getCustomParam())) {
                for (ColorApiParamDto colorCustom : param.getCustomParam()) {
                    if(colorCustom.getType()!=3) {
                        if (null != filter && filter.getSource() != null && filter.getSource() != colorCustom.getSource()) {
                            continue;
                        }
                        if (null != filter && filter.getIsNecessary() != null && filter.getIsNecessary() != colorCustom.getIsAppNecessary()) {
                            continue;
                        }
                    }
                    ColorGatewayParam custom = new ColorGatewayParam();
                    BeanUtils.copyProperties(colorCustom, custom);
                    custom.setDefaultShow(getDefaultShow(custom.getName()));
                    params.add(custom);
                }
            }
        }
        return params;
    }


    /**
     * 网关参数转换为页面结构jsonType
     * @param parameter
     * @return
     */
    private JsonType parameterToJsonType(ColorGatewayParam parameter,String zone){
        BuilderJsonType jsonType = new BuilderJsonType();
        if(zone.equals("pre")){
            jsonType.setName(parameter.getName());
            jsonType.setDataZone(2);
        }else{
            jsonType.setName(parameter.getName());
            jsonType.setDataZone(1);
        }
        jsonType.setRequired(parameter.getIsAppNecessary()==1);
        jsonType.setDesc(parameter.getDescription());
        jsonType.setType(parameter.getDataType());
        jsonType.setSource(ColorGatewayEnumDTO.getSourceInstance(parameter.getSource()));
        jsonType.setMark(ColorGatewayEnumDTO.getMark(parameter.getMark()));
        Map<String,ColorGatewayEnumDTO> trans=new HashMap<String,ColorGatewayEnumDTO>();
        trans.put(zone,ColorGatewayEnumDTO.getSimpleYesOrNo(parameter.getIsTransparent()));
        jsonType.setIsTransparent(trans);
        jsonType.setIsAppNecessary(ColorGatewayEnumDTO.getSimpleYesOrNo(parameter.getIsAppNecessary()));
        jsonType.setColorType(parameter.getType());
        jsonType.setDefaultShow(parameter.getDefaultShow());
        return jsonType;
    }


}
