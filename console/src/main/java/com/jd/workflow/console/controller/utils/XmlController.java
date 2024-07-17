package com.jd.workflow.console.controller.utils;

import com.jd.common.util.StringUtils;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.FlowDebugDto;
import com.jd.workflow.console.service.DebugService;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.XmlUtils;
import com.jd.workflow.soap.common.xml.XNode;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.ApiOperation;

@RestController
@Slf4j
@RequestMapping("/xml")
@UmpMonitor
@Api(tags = "xml管理")
public class XmlController {

    @Autowired
    DebugService debugService;

    @PostMapping(value = "/formatXml")
    @ResponseBody
    @ApiOperation(value = "xml格式化")
    public CommonResult<String> formatXml(@RequestBody String xml) {
        if (StringUtils.isEmpty(xml)) {
            return CommonResult.buildSuccessResult("");
        }
        boolean hasXmlDeclaration = xml.indexOf("<?xml") != -1;
        Document document = XmlUtils.parseXml(xml, false);
        try {
            return CommonResult.buildSuccessResult(XmlUtils.writeXml(document, hasXmlDeclaration));
        } catch (Exception e) {
            log.error("xml.err_format_xml", e);
            return CommonResult.error("格式化失败:" + e.getMessage());
        }
    }

    @PostMapping(value = "/parseXml")
    @ResponseBody
    @ApiOperation(value = "xml解析")
    public CommonResult<JsonType> parseXml(@RequestBody String xml) {
        XNode node = null;
        try {
            node = XNode.parse(xml);
        } catch (Exception e) {
            log.error("parseXml error", e);
            throw new BizException("解析xml失败");
        }
        if (node == null) {
            return CommonResult.buildSuccessResult(null);
        }
        return CommonResult.buildSuccessResult(node.toJsonType());
    }
}
