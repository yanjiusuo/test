package com.jd.workflow.console.service.doc;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.app.AppAllInfo;
import com.jd.workflow.console.dto.doc.DocReportDto;
import com.jd.workflow.console.dto.doc.EnumsReportDTO;
import com.jd.workflow.console.dto.doc.JavaBeanReportDto;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.doc.AppDocReportRecord;
import com.jd.workflow.console.entity.parser.InterfaceInfoDown;
import com.jd.workflow.console.model.sync.InterfaceJsonInfo;
import com.jd.workflow.console.service.listener.InterfaceChangeListener;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface IDocReportService extends IService<AppDocReportRecord> {
    public Long reportDoc(DocReportDto dto);
    void reportDocHashPrivilege(DocReportDto dto);
    public boolean reportJavaBean(JavaBeanReportDto dto);

    public AppAllInfo loadAppInfo(String appCode,String appSecret,String ip);

    public void addListener(InterfaceChangeListener interfaceChangeListener);

    Map<String, List<MethodManage>> parseFile(MultipartFile file);

    Map<String, List<MethodManage>> parseUrl(String url);

    /**
     * 上报枚举
     * @param dto
     * @return
     */
    boolean reportEnums(EnumsReportDTO dto);

    /**
     * 从JSF平台同步接口文档数据
     *
     * @param interfaceJsonInfo
     */
    void syncDocFromJsfPlatform(@RequestBody InterfaceJsonInfo interfaceJsonInfo);

    Long reportDocFromJsfPlatform(DocReportDto dto);

    /**
     * T+1 同步 JSF平台增量数据
     */
    public void syncDocFromJsfPlatformWorkerT1();
}
