package com.fanap.midhco.ui.component.multiAjaxFileUpload;

import java.io.Serializable;

public class UploadedFileInfo implements Serializable {
    String fileName;
    String fileId;
    String physicalLocation;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getPhysicalLocation() {
        return physicalLocation;
    }

    public void setPhysicalLocation(String physicalLocation) {
        this.physicalLocation = physicalLocation;
    }
}
