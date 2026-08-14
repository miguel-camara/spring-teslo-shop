package com.teslo.shop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String hostApi;
    private String uploadDir = "/static/products";
    private String uploadDirPdf = "/static/pdf";

    public String getHostApi() {
        return hostApi;
    }

    public void setHostApi(String hostApi) {
        this.hostApi = hostApi;
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getUploadDirPdf() {
        return uploadDirPdf;
    }

    public void setUploadDirPdf(String uploadDirPdf) {
        this.uploadDirPdf = uploadDirPdf;
    }
}
