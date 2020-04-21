package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.entities.File;
import com.fanap.midhco.appstore.entities.TimeLineFileType;
import com.fanap.midhco.appstore.service.osType.IUploadFilter;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

/**
 * Created by A.Moshiri on 8/12/2018.
 */
public class FileVO implements Serializable{

    public FileVO(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);

        try {
            if (jsonObject.has("fileId") && !jsonObject.get("fileId").equals(JSONObject.NULL))
                this.fileId = jsonObject.getLong("fileId");
            if (jsonObject.has("fileName") && !jsonObject.getString("fileName").trim().equals(""))
                this.fileName = jsonObject.getString("fileName");
            if (jsonObject.has("fileKey") && !jsonObject.getString("fileKey").trim().equals(""))
                this.fileKey = jsonObject.getString("fileKey");

        } finally {

        }

    }

    public FileVO() {
    }
    public String fileKey;
    public String fileName;
    public Long fileId;
    public Long fileType;
    boolean selected;



    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Long getFileType() {
        return fileType;
    }

    public void setFileType(Long fileType) {
        this.fileType = fileType;
    }

    public static FileVO convertFileToFileVo(File file) {
        if (file==null) {
            return null;
        }else {
            FileVO fileVO = new FileVO();
            fileVO.setFileKey(file.getFilePath());
            fileVO.setFileName(file.getFileName());
            fileVO.setFileId(file.getId());
            String fileExtension = "";
            fileExtension = file.getFileName().substring(file.getFileName().lastIndexOf(".")+1,file.getFileName().length());
            List<String> imageExtensionList = IUploadFilter.getImageUploadFilter().getFilterList();
            List<String> videoExtensionList = IUploadFilter.getVideoUploadFilter().getFilterList();
            List<String> htmlExtensionList = IUploadFilter.getHTMLUploadFilter().getFilterList();

            if(imageExtensionList.contains(fileExtension)){
                fileVO.setFileType(TimeLineFileType.IMAGE.getState());
            }else if(videoExtensionList.contains(fileExtension)){
                fileVO.setFileType(TimeLineFileType.VIDEO.getState());
            }else if(htmlExtensionList.contains(fileExtension)){
                fileVO.setFileType(TimeLineFileType.HTML.getState());
            }else {
                fileVO.setFileType(TimeLineFileType.ANY_FILE.getState());
            }
            return fileVO;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileVO fileVO = (FileVO) o;

        return fileId.equals(fileVO.fileId);
    }

    @Override
    public int hashCode() {
        return fileId.hashCode();
    }
}
