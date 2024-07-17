package com.jd.workflow.console.controller;

import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.MethodGroupTreeModel;
import com.jd.workflow.console.dto.doc.*;
import com.jd.workflow.console.dto.group.GroupResolveDto;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.export.DocExportService;
import com.jd.workflow.soap.common.exception.BizException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/export")
@UmpMonitor
@Api(value = "文档导出", tags = "文档导出")
public class DocExportController {
    @Autowired
    DocExportService docExportService;
    @Autowired
    IInterfaceManageService interfaceManageService;

    private void setCorsHeader(HttpServletResponse response, HttpServletRequest request) {

        String origin = request.getHeader("Origin");
        if (StringUtils.isNotEmpty(origin)) {
            response.addHeader("Access-Control-Allow-Origin", origin);
        } else {
            response.addHeader("Access-Control-Allow-Origin", "*");
        }

        response.addHeader("Access-Control-Allow-Methods", "GET,POST,HEAD,PUT,DELETE");
        response.addHeader("Access-Control-Allow-Headers", "Accept,Origin,X-Requested-With,Content-Type,Last-Modified");
        response.addHeader("Access-Control-Allow-Credentials", "true");

    }

    /**
     * 添加应用
     *
     * @param dto        接口id
     * @param sortModels 选中的文件夹以及方法信息
     * @return
     */
    @RequestMapping("/exportMd")
    @ApiOperation("导出markdown")
    public void exportMd(GroupResolveDto dto, @RequestBody(required = false) List<InterfaceSortModel> sortModels, HttpServletResponse response, HttpServletRequest request) {

        String outputResult = docExportService.exportMd(sortModels);
        response.reset();
        setCorsHeader(response, request);
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setContentLength((int) outputResult.length());
        response.setHeader("Content-Disposition", "attachment;filename=export.md");

        try (BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(outputResult.getBytes("utf-8")));) {
            byte[] buff = new byte[1024];
            OutputStream os = response.getOutputStream();
            int i = 0;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }
        } catch (IOException e) {
            log.error("export.err_export_md", e);
            throw new BizException("下载失败,请联系管理员", e);
        }

    }

    @RequestMapping("/exportCode")
    @ApiOperation("导出sdk")
    public void exportSdk(String sdkType, @RequestBody(required = false) List<TreeSortModel> sortModels, HttpServletResponse response, HttpServletRequest request) {
        /*InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
        MethodGroupTreeModel sortGroupTree = interfaceManage.getSortGroupTree();*/

     /*   if(sortModels == null){
            sortModels = sortGroupTree.getTreeItems();
        }*/
        File zipFile = docExportService.exportSdk(sdkType, sortModels);
        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        //response.setContentLength((int) outputResult.length());
        response.setHeader("Content-Disposition", "attachment;filename=export.zip");
        setCorsHeader(response, request);
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(zipFile));) {
            byte[] buff = new byte[1024];
            OutputStream os = response.getOutputStream();
            int i = 0;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }
        } catch (IOException e) {
            log.error("export.err_export_md", e);
            throw new BizException("下载失败,请联系管理员", e);
        } finally {
            zipFile.delete();
        }

    }

    /**
     * @param dto        接口id
     * @param sortModels 选中的文件夹以及方法信息
     * @param response
     */
    @RequestMapping("/exportHtml")
    @ApiOperation("导出html")
    public void exportHtml(GroupResolveDto dto, @RequestBody(required = false) List<InterfaceSortModel> sortModels, HttpServletResponse response, HttpServletRequest request) {

        String outputResult = docExportService.exportHtml(sortModels);
        response.reset();
        setCorsHeader(response, request);
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        byte[] bytes = null;
        try {
            bytes = outputResult.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        response.setContentLength((int) bytes.length);
        response.setHeader("Content-Disposition", "attachment;filename=doc.html");

        try (BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(bytes));) {
            byte[] buff = new byte[1024];
            OutputStream os = response.getOutputStream();
            int i = 0;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);

            }
            os.flush();
            os.close();
        } catch (IOException e) {
            log.error("export.err_export_md", e);
            throw new BizException("下载失败,请联系管理员", e);
        }

    }

    /**
     * @param dto        接口id
     * @param sortModels 选中的文件夹以及方法信息
     * @param response
     */
    @RequestMapping("/exportPdf")
    @ApiOperation("导出pdf")
    public void exportPdf(GroupResolveDto dto, @RequestBody(required = false) List<InterfaceSortModel> sortModels, HttpServletResponse response, HttpServletRequest request) {


        response.reset();
        setCorsHeader(response, request);
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=doc.pdf");

        try {
            docExportService.exportPdf(sortModels, response.getOutputStream());
        } catch (Exception e) {
            throw new BizException("导出pdf失败,请请联系管理员", e);
        }


    }

    @RequestMapping(value = "/exportSdkZip", method = RequestMethod.GET)
    public void exportSdkZip(ExportZipDTO exportZipDTO, HttpServletResponse response, HttpServletRequest request) {
        /*InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
        MethodGroupTreeModel sortGroupTree = interfaceManage.getSortGroupTree();*/

     /*   if(sortModels == null){
            sortModels = sortGroupTree.getTreeItems();
        }*/

        if (StringUtils.isEmpty(exportZipDTO.getIds())) {
            throw new BizException("接口id为空");
        }
        GroupSortModel sortModel = new GroupSortModel();
        sortModel.setName("默认分组");
        String[] ids = exportZipDTO.getIds().split(",");
        for (String id : ids) {
            MethodSortModel treeSortModel = new MethodSortModel();
            treeSortModel.setId((Long.parseLong(id)));
            sortModel.getChildren().add(treeSortModel);
        }

        InterfaceSortModel interfaceSortModel = new InterfaceSortModel();
        interfaceSortModel.getChildren().add(sortModel);

        List<TreeSortModel> sortModels = Lists.newArrayList();
        sortModels.add(interfaceSortModel);
        File zipFile = docExportService.exportSdk(exportZipDTO.getSdkType(), sortModels);
        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        //response.setContentLength((int) outputResult.length());
        response.setHeader("Content-Disposition", "attachment;filename=export.zip");
        setCorsHeader(response, request);
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(zipFile));) {
            byte[] buff = new byte[1024];
            OutputStream os = response.getOutputStream();
            int i = 0;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }
        } catch (IOException e) {
            log.error("export.err_export_md", e);
            throw new BizException("下载失败,请联系管理员", e);
        } finally {
            zipFile.delete();
        }

    }
}
