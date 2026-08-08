package com.teslo.shop.files.service;

import com.teslo.shop.common.exception.ApiBadRequestException;
import com.teslo.shop.config.AppProperties;
import java.io.File;
import org.springframework.stereotype.Service;

@Service
public class FilesService {

    private final File uploadDir;
    private final File uploadDirPdf;

    public FilesService(AppProperties properties) {
        this.uploadDir = new File(properties.getUploadDir());
        this.uploadDirPdf = new File(properties.getUploadDirPdf());
        System.out.println("---------------------------");
        System.out.println(
            "this.uploadDir = " + this.uploadDir.getAbsolutePath()
        );
        System.out.println(
            "this.uploadDirPdf = " + this.uploadDirPdf.getAbsolutePath()
        );
        System.out.println("---------------------------");
    }

    public File getStaticProductImage(String imageName) {
        System.out.println(this.uploadDir.getAbsolutePath());
        File file = new File(this.uploadDir, imageName);
        if (!file.exists()) {
            throw new ApiBadRequestException(
                "No product found with image " + imageName
            );
        }
        return file;
    }

    public File getStaticProductPdf(String pdfName) {
        System.out.println(this.uploadDirPdf.getAbsolutePath());
        File file = new File(this.uploadDirPdf, pdfName);
        if (!file.exists()) {
            throw new ApiBadRequestException("Not found with pdf " + pdfName);
        }
        return file;
    }

    public File getUploadDir() {
        return uploadDir;
    }

    public File getUploadDirPdf() {
        return uploadDirPdf;
    }
}
