package com.teslo.shop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String hostApi;
    private String uploadDir = "/static/products";
    private String uploadDirPdf = "/static/pdf";

    private SocketIo socketio = new SocketIo();

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

    public SocketIo getSocketio() {
        return socketio;
    }

    public void setSocketio(SocketIo socketio) {
        this.socketio = socketio;
    }

    public String getUploadDirPdf() {
        return uploadDirPdf;
    }

    public void setUploadDirPdf(String uploadDirPdf) {
        this.uploadDirPdf = uploadDirPdf;
    }

    public static class SocketIo {

        private int port = 3002;

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}
