package com.jd.workflow.server.dto;

/**
 * 文档导出
 */
public class DocExportDto {
    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 文件内容，utf-8编码
     */
    private String downloadUrl;

    public DocExportDto(String fileName,String downloadUrl) {
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
    }

    public DocExportDto() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
